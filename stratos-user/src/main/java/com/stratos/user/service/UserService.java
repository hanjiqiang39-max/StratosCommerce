package com.stratos.user.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.JwtUtil;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.common.constant.CacheConstants;
import com.stratos.user.config.OauthProperties;
import com.stratos.user.dto.SmsLoginDTO;
import com.stratos.user.dto.SmsRegisterDTO;
import com.stratos.user.dto.UserLoginDTO;
import com.stratos.user.dto.UserProfileDTO;
import com.stratos.user.dto.UserRegisterDTO;
import com.stratos.user.vo.AuthOptionsVO;
import com.stratos.user.entity.User;
import com.stratos.user.entity.UserAuth;
import com.stratos.user.entity.UserLevel;
import com.stratos.user.entity.UserLoginLog;
import com.stratos.user.entity.UserPointsAccount;
import com.stratos.user.entity.UserPointsLog;
import com.stratos.user.mapper.UserAuthMapper;
import com.stratos.user.mapper.UserLevelMapper;
import com.stratos.user.mapper.UserLoginLogMapper;
import com.stratos.user.mapper.UserMapper;
import com.stratos.user.mapper.UserPointsAccountMapper;
import com.stratos.user.mapper.UserPointsLogMapper;
import com.stratos.user.vo.LoginVO;
import com.stratos.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import com.stratos.common.result.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserLevelMapper userLevelMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final UserPointsAccountMapper userPointsAccountMapper;
    private final UserPointsLogMapper userPointsLogMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestTemplate restTemplate;
    private final OauthProperties oauthProperties;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${stratos.notification.base-url:http://localhost:8092}")
    private String notificationBaseUrl;

    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO dto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
        }

        if (StringUtils.hasText(dto.getPhone())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhoneHash, DigestUtil.sha256Hex(dto.getPhone()));
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException(ResultCode.USER_PHONE_EXISTS);
            }
        }

        if (StringUtils.hasText(dto.getEmail())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmailHash, DigestUtil.sha256Hex(dto.getEmail()));
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException(ResultCode.USER_EMAIL_EXISTS);
            }
        }

        User user = new User();
        Long userId = snowflakeIdGenerator.nextId();
        user.setId(userId);
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        user.setLevelId(1L);
        user.setPoints(0);
        user.setGrowthValue(0);
        user.setRegisterSource(0);

        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhoneEncrypted(dto.getPhone());
            user.setPhoneHash(DigestUtil.sha256Hex(dto.getPhone()));
        }
        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmailEncrypted(dto.getEmail());
            user.setEmailHash(DigestUtil.sha256Hex(dto.getEmail()));
        }

        userMapper.insert(user);

        UserAuth userAuth = new UserAuth();
        userAuth.setId(snowflakeIdGenerator.nextId());
        userAuth.setUserId(userId);
        userAuth.setAuthType(1);
        userAuth.setIdentifier(dto.getUsername());
        userAuth.setCredential(passwordEncoder.encode(dto.getPassword()));
        userAuth.setVerified(1);
        userAuthMapper.insert(userAuth);

        UserPointsAccount account = new UserPointsAccount();
        account.setId(snowflakeIdGenerator.nextId());
        account.setUserId(userId);
        account.setTotalPoints(0);
        account.setAvailablePoints(0);
        account.setUsedPoints(0);
        account.setFrozenPoints(0);
        account.setExpirePoints(0);
        account.setVersion(0);
        userPointsAccountMapper.insert(account);

        return userId;
    }

    public LoginVO login(UserLoginDTO dto, String loginIp, String userAgent) {
        LambdaQueryWrapper<UserAuth> authQuery = new LambdaQueryWrapper<>();
        authQuery.eq(UserAuth::getAuthType, 1)
                .eq(UserAuth::getIdentifier, dto.getUsername());
        UserAuth userAuth = userAuthMapper.selectOne(authQuery);

        if (userAuth == null || !passwordEncoder.matches(dto.getPassword(), userAuth.getCredential())) {
            saveLoginLog(userAuth != null ? userAuth.getUserId() : 0L, loginIp, userAgent, 2, "用户名或密码错误");
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        User user = userMapper.selectById(userAuth.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }
        if (user.getStatus() == 2) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_LOCKED);
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userMapper.updateById(user);
        saveLoginLog(user.getId(), loginIp, userAgent, 1, null);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId());
        claims.put("userType", "buyer");

        LoginVO vo = new LoginVO();
        vo.setToken(JwtUtil.generateToken(String.valueOf(user.getId()), claims, null));
        vo.setRefreshToken(JwtUtil.generateRefreshToken(String.valueOf(user.getId()), null));
        vo.setExpireTime(LocalDateTime.now().plusDays(7));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return vo;
    }

    public void sendSmsCode(String phone) {
        String limitKey = CacheConstants.Captcha.SMS_LIMIT + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(limitKey))) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请1分钟后再试");
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        stringRedisTemplate.opsForValue().set(
                CacheConstants.Captcha.SMS_CAPTCHA + phone, code, CacheConstants.Expire.FIVE_MINUTES, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(limitKey, "1", CacheConstants.Expire.ONE_MINUTE, TimeUnit.SECONDS);

        Map<String, String> payload = new HashMap<>();
        payload.put("phone", phone);
        payload.put("content", "您的验证码是" + code + "，5分钟内有效");
        payload.put("templateParam", "{\"code\":\"" + code + "\"}");
        try {
            Result<Void> result = restTemplate.postForObject(
                    notificationBaseUrl + "/notification/sms/send", payload, Result.class);
            if (result == null || !result.isSuccess()) {
                throw new BusinessException(ResultCode.SMS_SEND_FAILED,
                        result == null ? "短信服务无响应" : result.getMessage());
            }
        } catch (BusinessException ex) {
            stringRedisTemplate.delete(CacheConstants.Captcha.SMS_CAPTCHA + phone);
            stringRedisTemplate.delete(limitKey);
            throw ex;
        } catch (Exception ex) {
            stringRedisTemplate.delete(CacheConstants.Captcha.SMS_CAPTCHA + phone);
            stringRedisTemplate.delete(limitKey);
            log.error("通知服务发短信失败 phone={}", phone, ex);
            throw new BusinessException(ResultCode.SMS_SEND_FAILED, "短信发送失败，请检查通知服务或阿里云短信配置");
        }
        if (!smsReady()) {
            log.info("[MOCK SMS] 未配置阿里云，验证码仅写日志 phone={} code={}", phone, code);
        }
    }

    public void consumeSmsCode(String phone, String code) {
        String cached = stringRedisTemplate.opsForValue().get(CacheConstants.Captcha.SMS_CAPTCHA + phone);
        if (!StringUtils.hasText(cached) || !cached.equals(code)) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(CacheConstants.Captcha.SMS_CAPTCHA + phone);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginVO smsRegister(SmsRegisterDTO dto, String loginIp, String userAgent) {
        consumeSmsCode(dto.getPhone(), dto.getCode());
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("m" + dto.getPhone());
        registerDTO.setPassword(dto.getPassword());
        registerDTO.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : "用户" + dto.getPhone().substring(7));
        registerDTO.setPhone(dto.getPhone());
        register(registerDTO);
        String phoneHash = DigestUtil.sha256Hex(dto.getPhone());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhoneHash, phoneHash));
        return issueLogin(user, loginIp, userAgent);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginVO smsLogin(SmsLoginDTO dto, String loginIp, String userAgent) {
        String cached = stringRedisTemplate.opsForValue().get(CacheConstants.Captcha.SMS_CAPTCHA + dto.getPhone());
        if (!StringUtils.hasText(cached) || !cached.equals(dto.getCode())) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(CacheConstants.Captcha.SMS_CAPTCHA + dto.getPhone());

        String phoneHash = DigestUtil.sha256Hex(dto.getPhone());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhoneHash, phoneHash));
        if (user == null) {
            UserRegisterDTO registerDTO = new UserRegisterDTO();
            registerDTO.setUsername("m" + dto.getPhone());
            registerDTO.setPassword("Sms" + dto.getPhone().substring(5));
            registerDTO.setNickname("用户" + dto.getPhone().substring(7));
            registerDTO.setPhone(dto.getPhone());
            Long userId = register(registerDTO);
            user = userMapper.selectById(userId);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }
        return issueLogin(user, loginIp, userAgent);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByOauth(String provider, String openId, String nickname, String avatar,
                                String loginIp, String userAgent) {
        int authType = "wechat".equals(provider) ? 3 : 4;
        UserAuth userAuth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getAuthType, authType)
                .eq(UserAuth::getIdentifier, openId));
        User user;
        if (userAuth == null) {
            user = new User();
            Long userId = snowflakeIdGenerator.nextId();
            user.setId(userId);
            user.setUsername(("wechat".equals(provider) ? "wx" : "ali") + Long.toUnsignedString(userId, 36));
            user.setNickname(StringUtils.hasText(nickname) ? nickname : ("wechat".equals(provider) ? "微信用户" : "支付宝用户"));
            user.setAvatar(avatar);
            user.setStatus(1);
            user.setLevelId(1L);
            user.setPoints(0);
            user.setGrowthValue(0);
            user.setRegisterSource("wechat".equals(provider) ? 3 : 4);
            userMapper.insert(user);

            userAuth = new UserAuth();
            userAuth.setId(snowflakeIdGenerator.nextId());
            userAuth.setUserId(userId);
            userAuth.setAuthType(authType);
            userAuth.setIdentifier(openId);
            userAuth.setCredential("");
            userAuth.setVerified(1);
            userAuthMapper.insert(userAuth);

            UserPointsAccount account = new UserPointsAccount();
            account.setId(snowflakeIdGenerator.nextId());
            account.setUserId(userId);
            account.setTotalPoints(0);
            account.setAvailablePoints(0);
            account.setUsedPoints(0);
            account.setFrozenPoints(0);
            account.setExpirePoints(0);
            account.setVersion(0);
            userPointsAccountMapper.insert(account);
        } else {
            user = userMapper.selectById(userAuth.getUserId());
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            if (StringUtils.hasText(nickname) && !StringUtils.hasText(user.getNickname())) {
                user.setNickname(nickname);
            }
            if (StringUtils.hasText(avatar) && !StringUtils.hasText(user.getAvatar())) {
                user.setAvatar(avatar);
            }
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }
        return issueLogin(user, loginIp, userAgent);
    }

    public AuthOptionsVO authOptions() {
        AuthOptionsVO vo = new AuthOptionsVO();
        Map<String, Object> sms = smsStatus();
        vo.setSmsReady(Boolean.TRUE.equals(sms.get("ready")));
        vo.setSmsVendor(sms.get("vendor") == null ? "mock" : String.valueOf(sms.get("vendor")));
        vo.setWechatReady(oauthProperties.getWechat().ready());
        vo.setAlipayReady(oauthProperties.getAlipay().ready());
        return vo;
    }

    private LoginVO issueLogin(User user, String loginIp, String userAgent) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userMapper.updateById(user);
        saveLoginLog(user.getId(), loginIp, userAgent, 1, null);
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId());
        claims.put("userType", "buyer");
        LoginVO vo = new LoginVO();
        vo.setToken(JwtUtil.generateToken(String.valueOf(user.getId()), claims, null));
        vo.setRefreshToken(JwtUtil.generateRefreshToken(String.valueOf(user.getId()), null));
        vo.setExpireTime(LocalDateTime.now().plusDays(7));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return vo;
    }

    private boolean smsReady() {
        return Boolean.TRUE.equals(smsStatus().get("ready"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> smsStatus() {
        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                    notificationBaseUrl + "/notification/sms/status",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});
            Result<Map<String, Object>> body = response.getBody();
            if (body != null && body.isSuccess() && body.getData() != null) {
                return body.getData();
            }
        } catch (Exception ex) {
            log.warn("读取短信通道状态失败: {}", ex.getMessage());
        }
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("ready", false);
        fallback.put("vendor", "unknown");
        return fallback;
    }

    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        if (user.getPhoneEncrypted() != null) {
            userVO.setPhone(maskPhone(user.getPhoneEncrypted()));
        }
        if (user.getEmailEncrypted() != null) {
            userVO.setEmail(maskEmail(user.getEmailEncrypted()));
        }
        return userVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            user.setBirthday(dto.getBirthday());
        }
        userMapper.updateById(user);
    }

    public List<UserLevel> listLevels() {
        return userLevelMapper.selectList(new LambdaQueryWrapper<UserLevel>()
                .eq(UserLevel::getStatus, 1)
                .orderByAsc(UserLevel::getSortOrder));
    }

    public UserLevel getUserLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getLevelId() == null) {
            return null;
        }
        return userLevelMapper.selectById(user.getLevelId());
    }

    public UserPointsAccount getPointsAccount(Long userId) {
        return userPointsAccountMapper.selectOne(new LambdaQueryWrapper<UserPointsAccount>()
                .eq(UserPointsAccount::getUserId, userId));
    }

    public PageResult<UserVO> adminList(PageQuery pageQuery, String keyword, Integer status) {
        Page<User> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(status != null, User::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        query.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, query);
        List<UserVO> records = result.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            if (user.getPhoneEncrypted() != null) {
                vo.setPhone(maskPhone(user.getPhoneEncrypted()));
            }
            if (user.getEmailEncrypted() != null) {
                vo.setEmail(maskEmail(user.getEmailEncrypted()));
            }
            return vo;
        }).toList();
        return new PageResult<>(records, result.getTotal(), pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "状态不合法");
        }
        int rows = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .set(User::getStatus, status)
                .eq(User::getId, userId));
        if (rows == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新用户状态失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, Integer points, Long orderId) {
        if (points == null || points <= 0) {
            return;
        }
        UserPointsAccount account = getPointsAccount(userId);
        if (account == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "积分账户不存在");
        }
        int available = account.getAvailablePoints() == null ? 0 : account.getAvailablePoints();
        if (available < points) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "积分不足");
        }
        int rows = userPointsAccountMapper.update(null, new LambdaUpdateWrapper<UserPointsAccount>()
                .setSql("available_points = available_points - " + points)
                .setSql("used_points = used_points + " + points)
                .eq(UserPointsAccount::getUserId, userId)
                .ge(UserPointsAccount::getAvailablePoints, points));
        if (rows == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "积分不足");
        }
        User user = userMapper.selectById(userId);
        if (user != null) {
            int current = user.getPoints() == null ? 0 : user.getPoints();
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .set(User::getPoints, Math.max(current - points, 0))
                    .eq(User::getId, userId));
        }
        UserPointsLog log = new UserPointsLog();
        log.setId(snowflakeIdGenerator.nextId());
        log.setUserId(userId);
        log.setChangeType(2);
        log.setChangePoints(-points);
        log.setBeforePoints(available);
        log.setAfterPoints(available - points);
        log.setSourceType(2);
        log.setSourceId(orderId);
        log.setRemark("下单抵扣");
        log.setCreateTime(LocalDateTime.now());
        userPointsLogMapper.insert(log);
    }

    private void saveLoginLog(Long userId, String loginIp, String userAgent, Integer status, String failReason) {
        UserLoginLog log = new UserLoginLog();
        log.setId(snowflakeIdGenerator.nextId());
        log.setUserId(userId);
        log.setLoginType(1);
        log.setLoginIp(loginIp == null ? "unknown" : loginIp);
        log.setUserAgent(userAgent);
        log.setDeviceType(0);
        log.setLoginStatus(status);
        log.setFailReason(failReason);
        log.setLoginTime(LocalDateTime.now());
        userLoginLogMapper.insert(log);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return "*@" + parts[1];
        }
        return localPart.substring(0, 2) + "***@" + parts[1];
    }

}
