package com.stratos.common.constant;

/**
 * Redis 缓存键常量
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface CacheConstants {

    /**
     * 缓存键分隔符
     */
    String CACHE_KEY_SEPARATOR = ":";

    /**
     * 缓存过期时间（秒）
     */
    interface Expire {
        /** 1分钟 */
        long ONE_MINUTE = 60L;
        /** 5分钟 */
        long FIVE_MINUTES = 300L;
        /** 10分钟 */
        long TEN_MINUTES = 600L;
        /** 30分钟 */
        long THIRTY_MINUTES = 1800L;
        /** 1小时 */
        long ONE_HOUR = 3600L;
        /** 1天 */
        long ONE_DAY = 86400L;
        /** 7天 */
        long ONE_WEEK = 604800L;
        /** 30天 */
        long ONE_MONTH = 2592000L;
    }

    /**
     * 用户相关缓存键
     */
    interface User {
        /** 用户信息：user:info:{userId} */
        String USER_INFO = "user:info:";
        /** 用户Token：user:token:{userId} */
        String USER_TOKEN = "user:token:";
        /** 用户会话：user:session:{token} */
        String USER_SESSION = "user:session:";
    }

    /**
     * 商品相关缓存键
     */
    interface Product {
        /** 商品详情：product:info:{productId} */
        String PRODUCT_INFO = "product:info:";
        /** 商品库存：product:stock:{productId} */
        String PRODUCT_STOCK = "product:stock:";
        /** 商品分类：product:category:{categoryId} */
        String PRODUCT_CATEGORY = "product:category:";
        /** 分类树：product:category:tree */
        String CATEGORY_TREE = "product:category:tree";
    }

    /**
     * 订单相关缓存键
     */
    interface Order {
        /** 订单信息：order:info:{orderId} */
        String ORDER_INFO = "order:info:";
        /** 用户购物车：order:cart:{userId} */
        String USER_CART = "order:cart:";
    }

    /**
     * 验证码相关缓存键
     */
    interface Captcha {
        /** 图形验证码：captcha:image:{key} */
        String IMAGE_CAPTCHA = "captcha:image:";
        /** 短信验证码：captcha:sms:{phone} */
        String SMS_CAPTCHA = "captcha:sms:";
        /** 短信发送频率：captcha:sms:limit:{phone} */
        String SMS_LIMIT = "captcha:sms:limit:";
        /** 搜索热词：search:hot */
        String SEARCH_HOT = "search:hot";
        /** 邮箱验证码：captcha:email:{email} */
        String EMAIL_CAPTCHA = "captcha:email:";
    }

    /**
     * 限流相关缓存键
     */
    interface RateLimit {
        /** 接口限流：ratelimit:api:{method}:{ip} */
        String API_RATE_LIMIT = "ratelimit:api:";
        /** 用户操作限流：ratelimit:user:{userId}:{action} */
        String USER_RATE_LIMIT = "ratelimit:user:";
    }

    /**
     * 库存相关缓存键（值为纯数字字符串，供 INCR/DECR 使用）
     */
    interface Inventory {
        /** 可售库存：inventory:stock:{skuId} */
        String STOCK = "inventory:stock:";
    }

    /**
     * 秒杀相关缓存键
     */
    interface Seckill {
        /** 秒杀库存：seckill:stock:{seckillId} */
        String STOCK = "seckill:stock:";
        /** 用户已购：seckill:user:{seckillId}:{userId} */
        String USER = "seckill:user:";
    }

    /**
     * 分布式锁相关缓存键
     */
    interface Lock {
        /** 库存锁：lock:stock:{productId} */
        String STOCK_LOCK = "lock:stock:";
        /** 订单锁：lock:order:{orderId} */
        String ORDER_LOCK = "lock:order:";
        /** 优惠券锁：lock:coupon:{couponId} */
        String COUPON_LOCK = "lock:coupon:";
    }

}
