package com.stratos.common.util;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public class JwtUtil {

    /** 默认密钥（实际使用应从配置读取） */
    private static final String DEFAULT_SECRET = "StratosCommerce-JWT-Secret-Key-2024-Minimum-256-Bits-Required";

    /** Token过期时间（默认7天） */
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /** 刷新Token过期时间（默认30天） */
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 30 * 24 * 60 * 60 * 1000L;

    /**
     * 生成Token
     */
    public static String generateToken(String subject, Map<String, Object> claims, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(getSecret(secret).getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(key)
                .compact();
    }

    /**
     * 生成刷新Token
     */
    public static String generateRefreshToken(String subject, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(getSecret(secret).getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(key)
                .compact();
    }

    /**
     * 解析Token
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(getSecret(secret).getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否过期
     */
    public static boolean isTokenExpired(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从Token获取用户ID
     */
    public static String getUserIdFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.getSubject();
    }

    /**
     * 从Token获取自定义声明
     */
    public static Object getClaimFromToken(String token, String claimKey, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get(claimKey);
    }

    /**
     * 获取密钥
     */
    private static String getSecret(String secret) {
        return StrUtil.isNotBlank(secret) ? secret : DEFAULT_SECRET;
    }

}
