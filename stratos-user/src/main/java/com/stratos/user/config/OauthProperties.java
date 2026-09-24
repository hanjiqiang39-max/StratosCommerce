package com.stratos.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {

    private String frontendCallback = "http://localhost:5173/oauth/callback";

    private Wechat wechat = new Wechat();

    private Alipay alipay = new Alipay();

    @Data
    public static class Wechat {
        private String appId = "";
        private String appSecret = "";
        private String redirectUri = "http://localhost:8080/api/user/oauth/wechat/callback";

        public boolean ready() {
            return StringUtils.hasText(appId) && StringUtils.hasText(appSecret);
        }
    }

    @Data
    public static class Alipay {
        private boolean sandbox = true;
        private String appId = "";
        private String privateKey = "";
        private String alipayPublicKey = "";
        private String redirectUri = "http://localhost:8080/api/user/oauth/alipay/callback";
        private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

        public boolean ready() {
            return StringUtils.hasText(appId) && StringUtils.hasText(privateKey) && StringUtils.hasText(alipayPublicKey);
        }

        public String authorizeHost() {
            return sandbox ? "https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm"
                    : "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm";
        }
    }
}
