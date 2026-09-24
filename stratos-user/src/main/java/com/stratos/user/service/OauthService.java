package com.stratos.user.service;

import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratos.common.constant.CacheConstants;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.user.config.OauthProperties;
import com.stratos.user.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(OauthProperties.class)
public class OauthService {

    private static final String STATE_KEY = "oauth:state:";

    private final OauthProperties properties;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildAuthorizeUrl(String provider, String frontendRedirect) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String payload = provider + "|" + (StringUtils.hasText(frontendRedirect) ? frontendRedirect : "");
        stringRedisTemplate.opsForValue().set(STATE_KEY + state, payload, CacheConstants.Expire.TEN_MINUTES, TimeUnit.SECONDS);
        if ("wechat".equals(provider)) {
            if (!properties.getWechat().ready()) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "未配置微信开放平台 AppId/AppSecret，无法使用微信登录");
            }
            OauthProperties.Wechat wechat = properties.getWechat();
            return "https://open.weixin.qq.com/connect/qrconnect?appid=" + url(wechat.getAppId())
                    + "&redirect_uri=" + url(wechat.getRedirectUri())
                    + "&response_type=code&scope=snsapi_login&state=" + state + "#wechat_redirect";
        }
        if ("alipay".equals(provider)) {
            if (!properties.getAlipay().ready()) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "未配置支付宝开放平台密钥，无法使用支付宝登录");
            }
            OauthProperties.Alipay alipay = properties.getAlipay();
            return alipay.authorizeHost() + "?app_id=" + url(alipay.getAppId())
                    + "&scope=auth_user&redirect_uri=" + url(alipay.getRedirectUri())
                    + "&state=" + state;
        }
        throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "不支持的登录方式");
    }

    public String finishCallback(String provider, String code, String authCode, String state,
                                 String loginIp, String userAgent) {
        String payload = stringRedisTemplate.opsForValue().get(STATE_KEY + state);
        if (!StringUtils.hasText(payload) || !payload.startsWith(provider + "|")) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "登录状态已失效，请重试");
        }
        stringRedisTemplate.delete(STATE_KEY + state);
        String frontendRedirect = payload.substring(provider.length() + 1);
        OauthProfile profile = "wechat".equals(provider) ? wechatProfile(code) : alipayProfile(authCode);
        LoginVO vo = userService.loginByOauth(provider, profile.openId, profile.nickname, profile.avatar, loginIp, userAgent);
        String target = StringUtils.hasText(properties.getFrontendCallback())
                ? properties.getFrontendCallback() : "http://localhost:5173/oauth/callback";
        return target + "?token=" + url(vo.getToken())
                + "&userId=" + vo.getUserId()
                + "&username=" + url(vo.getUsername() == null ? "" : vo.getUsername())
                + "&nickname=" + url(vo.getNickname() == null ? "" : vo.getNickname())
                + "&redirect=" + url(frontendRedirect);
    }

    private OauthProfile wechatProfile(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "微信未返回授权码");
        }
        OauthProperties.Wechat wechat = properties.getWechat();
        String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + url(wechat.getAppId())
                + "&secret=" + url(wechat.getAppSecret())
                + "&code=" + url(code) + "&grant_type=authorization_code";
        JsonNode token = readJson(restTemplate.getForObject(tokenUrl, String.class));
        if (token.hasNonNull("errcode")) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "微信登录失败: " + token.path("errmsg").asText());
        }
        String openId = token.path("openid").asText();
        String accessToken = token.path("access_token").asText();
        if (!StringUtils.hasText(openId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "微信未返回用户标识");
        }
        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + url(accessToken)
                + "&openid=" + url(openId);
        JsonNode info = readJson(restTemplate.getForObject(infoUrl, String.class));
        OauthProfile profile = new OauthProfile();
        profile.openId = openId;
        profile.nickname = text(info, "nickname", "微信用户");
        profile.avatar = text(info, "headimgurl", "");
        return profile;
    }

    private OauthProfile alipayProfile(String authCode) {
        if (!StringUtils.hasText(authCode)) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "支付宝未返回授权码");
        }
        OauthProperties.Alipay alipay = properties.getAlipay();
        try {
            DefaultAlipayClient client = new DefaultAlipayClient(
                    alipay.getGatewayUrl(), alipay.getAppId(), alipay.getPrivateKey(),
                    "json", "UTF-8", alipay.getAlipayPublicKey(), "RSA2");
            AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
            tokenRequest.setGrantType("authorization_code");
            tokenRequest.setCode(authCode);
            AlipaySystemOauthTokenResponse token = client.execute(tokenRequest);
            if (token == null || !token.isSuccess()) {
                String msg = token == null ? "无响应" : token.getSubMsg() != null ? token.getSubMsg() : token.getMsg();
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付宝登录失败: " + msg);
            }
            String openId = StringUtils.hasText(token.getUserId()) ? token.getUserId() : token.getOpenId();
            OauthProfile profile = new OauthProfile();
            profile.openId = openId;
            profile.nickname = "支付宝用户";
            profile.avatar = "";
            if (StringUtils.hasText(token.getAccessToken())) {
                AlipayUserInfoShareRequest infoRequest = new AlipayUserInfoShareRequest();
                AlipayUserInfoShareResponse info = client.execute(infoRequest, token.getAccessToken());
                if (info != null && info.isSuccess()) {
                    profile.nickname = StringUtils.hasText(info.getNickName()) ? info.getNickName() : profile.nickname;
                    profile.avatar = info.getAvatar() == null ? "" : info.getAvatar();
                }
            }
            if (!StringUtils.hasText(profile.openId)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付宝未返回用户标识");
            }
            return profile;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝登录异常", e);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付宝登录失败: " + e.getMessage());
        }
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(body == null ? "{}" : body);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "解析第三方响应失败");
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node == null ? "" : node.path(field).asText();
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String url(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static class OauthProfile {
        private String openId;
        private String nickname;
        private String avatar;
    }
}
