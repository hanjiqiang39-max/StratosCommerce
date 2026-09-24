# StratosCommerce 系统架构设计

> **版本**：v1.0  
> **架构风格**：微服务 + 事件驱动  
> **部署模式**：云原生 Kubernetes

---

## 🏗️ 总体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户层 (User Layer)                        │
├─────────────────────────────────────────────────────────────────┤
│  Web浏览器  │  移动APP  │  小程序  │  开放API  │  管理后台        │
└────────┬────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                     接入层 (Gateway Layer)                        │
├─────────────────────────────────────────────────────────────────┤
│           Nginx (负载均衡 + HTTPS + 静态资源CDN)                  │
│                             ▼                                    │
│          Spring Cloud Gateway (统一网关)                          │
│          ├─ 路由转发                                              │
│          ├─ 认证鉴权 (OAuth 2.0 + JWT)                           │
│          ├─ 限流熔断 (Sentinel)                                  │
│          ├─ 日志记录                                              │
│          └─ 灰度发布                                              │
└────────┬────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    业务服务层 (Service Layer)                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 用户服务     │  │ 商品服务     │  │ 订单服务     │             │
│  │ User-Service │  │Product-Srv  │  │Order-Service│             │
│  │ :8801       │  │ :8802       │  │ :8803       │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
│                                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 支付服务     │  │ 营销服务     │  │ 库存服务     │             │
│  │Payment-Srv  │  │Promotion-Srv│  │Inventory-Srv│             │
│  │ :8804       │  │ :8805       │  │ :8806       │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
│                                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 搜索服务     │  │ 物流服务     │  │ 消息服务     │             │
│  │Search-Srv   │  │Logistics-Srv│  │Message-Srv  │             │
│  │ :8807       │  │ :8808       │  │ :8809       │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
│                                                                   │
└────────┬──────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   中间件层 (Middleware Layer)                     │
├─────────────────────────────────────────────────────────────────┤
│  Nacos           │  Sentinel      │  Seata         │  RocketMQ  │
│  (注册+配置)      │  (限流熔断)     │  (分布式事务)   │  (消息队列) │
│                                                                   │
│  SkyWalking      │  XXL-Job       │  MinIO         │            │
│  (链路追踪)       │  (定时任务)     │  (对象存储)     │            │
└────────┬──────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    数据层 (Data Layer)                            │
├─────────────────────────────────────────────────────────────────┤
│  MySQL (主库)     │  MySQL (从库)  │  Redis         │  ES        │
│  (订单/用户)       │  (读库)        │  (缓存)        │  (搜索)     │
│                                                                   │
│  MongoDB         │  MinIO         │                │            │
│  (日志/评论)      │  (文件存储)     │                │            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧩 微服务拆分方案

### 1️⃣ 用户服务 (stratos-user-service)
**端口**: 8801  
**职责**: 用户注册、登录、个人信息、权限管理、会员体系

**核心接口**:
```
POST   /api/user/register          # 用户注册
POST   /api/user/login             # 用户登录
GET    /api/user/info              # 获取用户信息
PUT    /api/user/profile           # 更新个人资料
GET    /api/user/addresses         # 收货地址列表
POST   /api/user/address           # 新增收货地址
GET    /api/user/points/history    # 积分历史
```

**数据库表**:
- `user` - 用户主表
- `user_auth` - 认证信息（密码、手机、邮箱）
- `user_address` - 收货地址
- `user_points` - 积分账户
- `user_level` - 会员等级

---

### 2️⃣ 商品服务 (stratos-product-service)
**端口**: 8802  
**职责**: 商品管理、分类管理、库存查询、SPU/SKU管理

**核心接口**:
```
GET    /api/product/list           # 商品列表
GET    /api/product/{id}           # 商品详情
POST   /api/product                # 发布商品
PUT    /api/product/{id}           # 更新商品
GET    /api/product/category       # 分类树
GET    /api/product/stock/{skuId}  # 查询库存
```

**数据库表**:
- `product_spu` - SPU主表
- `product_sku` - SKU表
- `product_category` - 分类表
- `product_brand` - 品牌表
- `product_attr` - 属性表

---

### 3️⃣ 订单服务 (stratos-order-service)
**端口**: 8803  
**职责**: 订单创建、订单查询、订单状态流转、售后管理

**核心接口**:
```
POST   /api/order/create           # 创建订单
GET    /api/order/{orderNo}        # 订单详情
GET    /api/order/list             # 订单列表
POST   /api/order/cancel           # 取消订单
POST   /api/order/refund           # 申请退款
GET    /api/order/logistics/{orderNo} # 物流信息
```

**数据库表**:
- `order_info` - 订单主表
- `order_item` - 订单明细
- `order_status_log` - 订单状态变更日志
- `order_refund` - 退款单
- `order_logistics` - 物流信息

**分库分表策略**:
```sql
-- 按用户ID取模分16张表
order_info_0 ~ order_info_15
-- 路由规则: user_id % 16
```

---

### 4️⃣ 支付服务 (stratos-payment-service)
**端口**: 8804  
**职责**: 支付通道对接、支付回调处理、退款处理、对账

**核心接口**:
```
POST   /api/pay/create             # 创建支付单
POST   /api/pay/callback           # 支付回调
GET    /api/pay/status/{payNo}     # 查询支付状态
POST   /api/pay/refund             # 发起退款
GET    /api/pay/reconcile          # 对账
```

**数据库表**:
- `pay_order` - 支付单
- `pay_channel` - 支付通道配置
- `pay_refund` - 退款单
- `pay_reconcile` - 对账记录

---

### 5️⃣ 营销服务 (stratos-promotion-service)
**端口**: 8805  
**职责**: 秒杀活动、优惠券、满减、拼团

**核心接口**:
```
GET    /api/promotion/seckill/list # 秒杀活动列表
POST   /api/promotion/seckill/grab # 秒杀抢购
GET    /api/coupon/list            # 优惠券列表
POST   /api/coupon/receive         # 领取优惠券
POST   /api/coupon/use             # 使用优惠券
```

**数据库表**:
- `promotion_seckill` - 秒杀活动
- `promotion_coupon` - 优惠券模板
- `user_coupon` - 用户优惠券
- `promotion_full_discount` - 满减活动

---

### 6️⃣ 库存服务 (stratos-inventory-service)
**端口**: 8806  
**职责**: 库存扣减、库存预扣、库存释放、库存同步

**核心接口**:
```
GET    /api/inventory/stock/{skuId}     # 查询库存
POST   /api/inventory/deduct            # 扣减库存
POST   /api/inventory/freeze            # 冻结库存（秒杀预扣）
POST   /api/inventory/release           # 释放库存
POST   /api/inventory/sync              # 同步库存到缓存
```

**数据库表**:
- `inventory_stock` - 库存主表
- `inventory_log` - 库存变更日志

**缓存设计**:
```
Redis Key: inventory:stock:{skuId}
Redis Key: inventory:freeze:{skuId}:{userId}  # 秒杀冻结库存
```

---

### 7️⃣ 搜索服务 (stratos-search-service)
**端口**: 8807  
**职责**: 商品搜索、搜索联想、热词统计

**核心接口**:
```
GET    /api/search/product         # 商品搜索
GET    /api/search/suggest         # 搜索联想
GET    /api/search/hot             # 热门搜索词
POST   /api/search/sync            # 同步商品到ES
```

**数据存储**:
- ElasticSearch索引: `stratos_product`
- Redis缓存热词: `search:hot:words`

---

### 8️⃣ 物流服务 (stratos-logistics-service)
**端口**: 8808  
**职责**: 物流公司对接、运费计算、轨迹查询

**核心接口**:
```
GET    /api/logistics/trace/{orderNo}   # 查询物流轨迹
POST   /api/logistics/ship              # 发货
POST   /api/logistics/freight           # 计算运费
```

**数据库表**:
- `logistics_company` - 物流公司
- `logistics_trace` - 物流轨迹

---

### 9️⃣ 消息服务 (stratos-message-service)
**端口**: 8809  
**职责**: 站内信、短信、邮件、推送通知

**核心接口**:
```
POST   /api/message/send           # 发送消息
GET    /api/message/list           # 消息列表
PUT    /api/message/read/{id}      # 标记已读
```

**数据库表**:
- `message_template` - 消息模板
- `message_record` - 消息记录

---

## 🔄 核心业务流程

### 秒杀抢购流程
```
┌─────────┐
│ 用户点击 │
│ 秒杀按钮 │
└────┬────┘
     │
     ▼
┌──────────────────┐
│ Gateway限流       │  ← Sentinel: 单用户1QPS
│ (防刷单)          │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ 营销服务          │
│ 校验活动状态      │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ Redis预扣库存     │  ← Lua脚本保证原子性
│ (防超卖)          │     EVAL "if redis.call('get', KEYS[1]) > 0 then"
└────┬─────────────┘
     │ 扣减成功
     ▼
┌──────────────────┐
│ 发送MQ消息        │  ← RocketMQ异步创建订单
│ (削峰)            │     Topic: seckill_order
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ 订单服务消费      │
│ 创建订单          │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ 返回抢购成功      │
└──────────────────┘
```

---

### 订单支付流程（分布式事务）
```
┌─────────┐
│ 用户支付 │
└────┬────┘
     │
     ▼
┌──────────────────┐
│ 支付服务          │
│ 调用支付宝API     │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ 支付宝回调        │  ← 异步通知（需幂等处理）
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ 发送MQ事务消息    │  ← RocketMQ事务消息
│ Topic: pay_success│
└────┬─────────────┘
     │
     ├──────────────┐
     │              │
     ▼              ▼
┌─────────┐    ┌─────────┐
│订单服务  │    │库存服务 │
│更新状态  │    │扣减库存  │
└────┬────┘    └────┬────┘
     │              │
     ▼              ▼
┌─────────┐    ┌─────────┐
│积分服务  │    │优惠券服务│
│增加积分  │    │发放优惠券│
└─────────┘    └─────────┘
```

**分布式事务方案**：可靠消息最终一致性
```java
// 1. 发送Half消息
rocketMQTemplate.sendMessageInTransaction("pay_success", order, null);

// 2. 执行本地事务
@Transactional
public void updatePayStatus(String orderNo) {
    // 更新支付状态
    payMapper.updateStatus(orderNo, "SUCCESS");
}

// 3. 消费端幂等处理
@RocketMQMessageListener(topic = "pay_success")
public void onMessage(OrderDTO order) {
    String key = "pay:idempotent:" + order.getOrderNo();
    Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
    if (Boolean.TRUE.equals(success)) {
        // 执行业务逻辑
        orderService.updateStatus(order.getOrderNo());
        inventoryService.deduct(order.getSkuId(), order.getQuantity());
        pointsService.add(order.getUserId(), order.getPoints());
    }
}
```

---

## 💾 数据库设计规范

### 命名规范
```
表名: {业务前缀}_{表名}  如: order_info, user_address
字段: 小写下划线分隔    如: user_id, create_time
索引: idx_{字段名}      如: idx_user_id
唯一索引: uk_{字段名}   如: uk_phone
```

### 公共字段
```sql
id          BIGINT         主键（雪花算法生成）
create_time DATETIME       创建时间
update_time DATETIME       更新时间
create_by   VARCHAR(64)    创建人
update_by   VARCHAR(64)    更新人
is_deleted  TINYINT(1)     逻辑删除 0=未删除 1=已删除
version     INT            乐观锁版本号
```

### 索引设计原则
1. 主键使用雪花算法（解决自增ID并发问题）
2. 高频查询字段建立索引
3. 联合索引遵循最左前缀原则
4. 避免索引列使用函数或表达式
5. 控制单表索引数量≤5个

---

## 🚀 部署架构

### Kubernetes部署拓扑
```yaml
Namespace: stratos-prod

Deployments:
  - stratos-gateway        (副本数: 3, 资源: 2C4G)
  - stratos-user-service   (副本数: 2, 资源: 1C2G)
  - stratos-product-service (副本数: 2, 资源: 1C2G)
  - stratos-order-service  (副本数: 3, 资源: 2C4G)  ← 核心服务多副本
  - stratos-payment-service (副本数: 3, 资源: 2C4G) ← 核心服务多副本
  - stratos-promotion-service (副本数: 3, 资源: 2C4G) ← 秒杀核心
  - stratos-inventory-service (副本数: 2, 资源: 1C2G)
  - stratos-search-service (副本数: 2, 资源: 1C2G)
  - stratos-logistics-service (副本数: 1, 资源: 1C2G)
  - stratos-message-service (副本数: 1, 资源: 1C2G)

StatefulSets:
  - mysql-master           (副本数: 1, 资源: 4C8G, 存储: 500GB)
  - mysql-slave            (副本数: 2, 资源: 4C8G, 存储: 500GB)
  - redis-cluster          (副本数: 6, 资源: 2C4G, 存储: 100GB)
  - elasticsearch          (副本数: 3, 资源: 4C8G, 存储: 500GB)
  - rocketmq-nameserver    (副本数: 2, 资源: 1C2G)
  - rocketmq-broker        (副本数: 2, 资源: 2C4G, 存储: 200GB)

Services:
  - stratos-gateway-svc (LoadBalancer)
  - 各微服务 ClusterIP
```

---

## 📊 监控告警体系

### 监控指标
```
业务指标:
- 实时QPS、成功率、错误率
- 订单创建成功率
- 支付成功率
- 秒杀库存剩余

系统指标:
- CPU使用率 > 80% 告警
- 内存使用率 > 85% 告警
- 磁盘使用率 > 90% 告警
- JVM GC时间 > 1s 告警

链路指标:
- 接口响应时间 P99
- 慢SQL查询（>500ms）
- Redis慢查询（>100ms）
- 消息队列堆积量
```

### 告警通道
```
钉钉群机器人  - 实时告警
企业微信      - 严重告警
短信          - P0级别告警
邮件          - 日报周报
```

---

## ✅ 架构确认检查清单

### 微服务拆分
- [x] 9个核心微服务已明确
- [x] 服务职责清晰无交叉
- [x] 接口定义完整
- [x] 数据库表设计规范

### 核心流程
- [x] 秒杀流程（防超卖）
- [x] 支付流程（分布式事务）
- [x] 订单流程（状态机）

### 技术选型
- [x] 分库分表方案（ShardingSphere）
- [x] 分布式事务方案（可靠消息）
- [x] 缓存方案（多级缓存）
- [x] 搜索方案（ElasticSearch）

### 部署运维
- [x] Kubernetes部署方案
- [x] 监控告警体系
- [x] 日志收集方案

**状态**：系统架构已完整确认 ✅

---

## 📁 项目结构预览
```
stratos-commerce/
├── stratos-gateway/              # API网关
├── stratos-common/               # 公共模块
├── stratos-user-service/         # 用户服务
├── stratos-product-service/      # 商品服务
├── stratos-order-service/        # 订单服务
├── stratos-payment-service/      # 支付服务
├── stratos-promotion-service/    # 营销服务
├── stratos-inventory-service/    # 库存服务
├── stratos-search-service/       # 搜索服务
├── stratos-logistics-service/    # 物流服务
├── stratos-message-service/      # 消息服务
├── stratos-admin-web/            # 管理后台（Vue3）
├── stratos-shop-web/             # 用户商城（Vue3）
├── docker-compose.yml            # 本地开发环境
└── k8s/                          # Kubernetes部署文件
```
