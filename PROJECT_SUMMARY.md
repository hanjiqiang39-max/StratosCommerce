# StratosCommerce 项目总览

> **最后更新**：2026-09-22  
> **状态**：✅ 技术栈已确认 | ✅ 业务需求已确认 | ✅ 系统架构已确认

---

## 📋 项目定位

**StratosCommerce**（平流层商业）是一个**企业级分布式商业交易平台**，专为中大型企业打造的高并发电商解决方案。

### 核心特点
- 🚀 **高性能**：支持10万+并发，P99响应<500ms
- 🔒 **高安全**：OAuth 2.0 + 数据加密 + WAF防护
- 💎 **高颜值**：Vue3 + Naive UI + 深色主题设计
- 🎯 **高完整度**：覆盖秒杀、支付、物流、营销全链路

---

## 📦 完整技术栈确认

### 后端技术（已确认 ✅）
| 分类 | 技术选型 |
|------|----------|
| **核心框架** | Spring Boot 3.2 + Spring Cloud Alibaba 2023 |
| **API网关** | Spring Cloud Gateway + Sentinel |
| **服务治理** | Nacos (注册+配置) + Sentinel (限流熔断) + Seata (分布式事务) |
| **数据访问** | MyBatis-Plus + ShardingSphere-JDBC |
| **安全认证** | Spring Security + OAuth 2.0 + JWT + Bcrypt |
| **消息队列** | RocketMQ 5.x |
| **任务调度** | XXL-Job |
| **链路追踪** | SkyWalking |
| **监控告警** | Prometheus + Grafana |

### 前端技术（已确认 ✅）
| 分类 | 技术选型 |
|------|----------|
| **核心框架** | Vue 3.4 + TypeScript 5.4 + Vite 5 |
| **UI组件** | Naive UI 2.38 + Tailwind CSS 3.4 |
| **状态管理** | Pinia 2.1 |
| **动画库** | GSAP 3.12 |
| **图标库** | Iconify |
| **数据可视化** | ECharts 5.5 |
| **HTTP请求** | Axios 1.6 |

### 数据存储（已确认 ✅）
| 分类 | 技术选型 | 用途 |
|------|----------|------|
| **关系数据库** | MySQL 8.0 + ShardingSphere | 订单/用户核心数据，支持分库分表 |
| **缓存** | Redis 7.x (哨兵模式) | 热点数据 + Session + 分布式锁 |
| **搜索引擎** | ElasticSearch 8.x | 商品全文检索 |
| **文档数据库** | MongoDB 7.x | 商品评论、用户行为日志 |
| **对象存储** | MinIO | 商品图片、用户头像 |

### 部署运维（已确认 ✅）
| 分类 | 技术选型 |
|------|----------|
| **容器化** | Docker + Docker Compose |
| **容器编排** | Kubernetes |
| **CI/CD** | Jenkins / GitHub Actions |
| **反向代理** | Nginx |
| **日志收集** | ELK Stack (Elasticsearch + Logstash + Kibana) |

### 网络安全体系（已确认 ✅）
- ✅ Spring Security + OAuth 2.0 + JWT 认证
- ✅ Bcrypt 密码加密 + AES 敏感数据加密
- ✅ HTTPS/TLS 1.3 传输加密
- ✅ Spring Validation 参数校验（防SQL注入/XSS）
- ✅ Sentinel 接口限流（防刷单/爬虫）
- ✅ CORS 跨域控制（防CSRF）
- ✅ SLF4J + Logback 审计日志
- ✅ 阿里云WAF / Cloudflare（DDoS防护）

完整详见 → [TECH_STACK.md](TECH_STACK.md)

---

## 🎯 业务需求确认

### 8大核心功能模块（已确认 ✅）

#### 1️⃣ 用户中心
- 注册登录（手机/邮箱/第三方）
- 权限管理（RBAC）
- 个人资料、收货地址
- 会员体系、积分系统

#### 2️⃣ 商品管理
- SPU/SKU管理
- 三级分类、品牌管理
- 库存管理（实时库存、预警、冻结）
- 商品搜索（ES全文检索）

#### 3️⃣ 订单交易
- 购物车、下单流程
- 多种支付方式（支付宝/微信/银联）
- 订单状态流转
- 售后服务（退款/退货）

#### 4️⃣ 营销促销
- **限时秒杀**（防超卖核心场景）
- 优惠券（满减券/折扣券）
- 满减活动、拼团
- 积分商城、分销推广

#### 5️⃣ 支付结算
- 多支付通道对接
- 支付安全（二次确认、风控）
- 异步通知处理（幂等性）
- 对账系统

#### 6️⃣ 物流配送
- 物流公司API对接
- 运费计算（按重量/区域）
- 物流轨迹查询

#### 7️⃣ 数据统计
- 实时看板（订单/销售/在线用户）
- 销售报表、运营分析
- 财务报表

#### 8️⃣ 系统管理
- 管理后台（角色权限、操作日志）
- 内容管理（轮播图、公告）
- 消息通知（站内信、短信、邮件）

### 3大高并发核心场景（已确认 ✅）

#### 场景1：秒杀抢购
```
前端：防抖节流 + 按钮锁定
网关：Sentinel限流（单用户1QPS）
缓存：Redis Lua脚本预扣库存（原子性）
队列：RocketMQ异步创建订单（削峰）
兜底：XXL-Job定时释放未支付库存
```

#### 场景2：订单支付（分布式事务）
```
方案：RocketMQ事务消息（可靠消息最终一致性）
流程：支付成功 → 更新订单 → 扣减库存 → 增加积分 → 发放优惠券
保证：幂等性处理（Redis setIfAbsent）
```

#### 场景3：商品搜索
```
技术：ElasticSearch 8.x + IK分词器 + 拼音分词器
目标：0.5秒内返回搜索结果
功能：模糊搜索、价格筛选、销量排序
```

### 非功能性需求（已确认 ✅）

| 指标类型 | 目标值 |
|----------|--------|
| **接口响应时间** | P95<200ms, P99<500ms |
| **QPS** | 单服务5000+ |
| **并发用户数** | 10万+ |
| **系统可用性** | 99.9% |
| **数据可靠性** | 99.999% |
| **页面加载速度** | 首屏<1.5s |

完整详见 → [BUSINESS_REQUIREMENTS.md](BUSINESS_REQUIREMENTS.md)

---

## 🏗️ 系统架构确认

### 4层架构设计（已确认 ✅）

```
┌────────────────────────────────────┐
│  接入层：Nginx + Gateway + Sentinel │
└──────────────┬─────────────────────┘
               │
┌──────────────┴─────────────────────┐
│  业务层：9个微服务                   │
│  用户/商品/订单/支付/营销/库存/搜索  │
└──────────────┬─────────────────────┘
               │
┌──────────────┴─────────────────────┐
│  中间件：Nacos/Seata/RocketMQ/Sky   │
└──────────────┬─────────────────────┘
               │
┌──────────────┴─────────────────────┐
│  数据层：MySQL/Redis/ES/MongoDB     │
└────────────────────────────────────┘
```

### 9个核心微服务（已确认 ✅）

| 服务名 | 端口 | 职责 |
|--------|------|------|
| **stratos-gateway** | 8080 | API网关、认证鉴权、限流熔断 |
| **stratos-user-service** | 8801 | 用户注册登录、权限管理、会员体系 |
| **stratos-product-service** | 8802 | 商品管理、分类管理、SPU/SKU |
| **stratos-order-service** | 8803 | 订单创建、状态流转、售后管理 |
| **stratos-payment-service** | 8804 | 支付通道对接、回调处理、退款对账 |
| **stratos-promotion-service** | 8805 | 秒杀活动、优惠券、满减活动 |
| **stratos-inventory-service** | 8806 | 库存扣减、预扣、释放、同步 |
| **stratos-search-service** | 8807 | 商品搜索、搜索联想、热词统计 |
| **stratos-logistics-service** | 8808 | 物流对接、运费计算、轨迹查询 |
| **stratos-message-service** | 8809 | 站内信、短信、邮件、推送通知 |

### 数据库设计规范（已确认 ✅）

#### 命名规范
```
表名：{业务前缀}_{表名}  如 order_info
字段：小写下划线        如 user_id, create_time
索引：idx_{字段名}      如 idx_user_id
唯一索引：uk_{字段名}   如 uk_phone
```

#### 公共字段
```sql
id          BIGINT         -- 雪花算法生成
create_time DATETIME       -- 创建时间
update_time DATETIME       -- 更新时间
create_by   VARCHAR(64)    -- 创建人
update_by   VARCHAR(64)    -- 更新人
is_deleted  TINYINT(1)     -- 逻辑删除
version     INT            -- 乐观锁版本号
```

#### 分库分表策略
```
订单表：按用户ID取模分16张表
路由规则：user_id % 16
表名：order_info_0 ~ order_info_15
```

### Kubernetes部署方案（已确认 ✅）

```yaml
核心服务（3副本）：
- stratos-gateway (3副本, 2C4G)
- stratos-order-service (3副本, 2C4G)
- stratos-payment-service (3副本, 2C4G)
- stratos-promotion-service (3副本, 2C4G) # 秒杀核心

普通服务（2副本）：
- stratos-user-service (2副本, 1C2G)
- stratos-product-service (2副本, 1C2G)
- stratos-inventory-service (2副本, 1C2G)
- stratos-search-service (2副本, 1C2G)

辅助服务（1副本）：
- stratos-logistics-service (1副本, 1C2G)
- stratos-message-service (1副本, 1C2G)

中间件：
- MySQL主从 (1主2从, 4C8G, 500GB)
- Redis集群 (6节点, 2C4G, 100GB)
- ElasticSearch (3节点, 4C8G, 500GB)
- RocketMQ (2NameServer + 2Broker)
```

完整详见 → [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md)

---

## 📁 项目结构预览

```
StratosCommerce/
├── README.md                      # 项目主文档 ✅
├── TECH_STACK.md                  # 完整技术栈方案 ✅
├── BUSINESS_REQUIREMENTS.md       # 业务需求文档 ✅
├── SYSTEM_ARCHITECTURE.md         # 系统架构设计 ✅
├── PROJECT_SUMMARY.md             # 本文件（项目总览）✅
│
├── stratos-gateway/               # API网关服务
├── stratos-common/                # 公共模块
│   ├── common-core/               # 核心工具类
│   ├── common-redis/              # Redis封装
│   ├── common-security/           # 安全组件
│   └── common-mq/                 # 消息队列封装
│
├── stratos-user-service/          # 用户服务
├── stratos-product-service/       # 商品服务
├── stratos-order-service/         # 订单服务
├── stratos-payment-service/       # 支付服务
├── stratos-promotion-service/     # 营销服务
├── stratos-inventory-service/     # 库存服务
├── stratos-search-service/        # 搜索服务
├── stratos-logistics-service/     # 物流服务
├── stratos-message-service/       # 消息服务
│
├── stratos-admin-web/             # 管理后台（Vue3）
├── stratos-shop-web/              # 用户商城（Vue3）
│
├── docker/                        # Docker配置
│   └── docker-compose.yml         # 本地开发环境
│
├── k8s/                           # Kubernetes部署文件
│   ├── namespace.yaml
│   ├── gateway-deployment.yaml
│   ├── services/                  # 各微服务部署文件
│   └── middleware/                # 中间件部署文件
│
├── docs/                          # 详细文档
│   ├── DATABASE_DESIGN.md         # 数据库设计
│   ├── API_DOCUMENTATION.md       # 接口文档
│   ├── DEPLOYMENT_GUIDE.md        # 部署手册
│   └── DEVELOPMENT_STANDARDS.md   # 开发规范
│
└── sql/                           # SQL脚本
    ├── schema/                    # 表结构
    └── data/                      # 初始数据
```

---

## ✅ 最终确认清单

### 技术栈（已完整确认 ✅）
- [x] 后端框架：Spring Boot 3.2 + Spring Cloud Alibaba
- [x] 前端框架：Vue 3 + TypeScript + Vite
- [x] UI组件：Naive UI + Tailwind CSS
- [x] 安全体系：OAuth 2.0 + JWT + 数据加密 + WAF
- [x] 数据存储：MySQL + Redis + ElasticSearch + MongoDB + MinIO
- [x] 中间件：Nacos + Sentinel + Seata + RocketMQ + SkyWalking
- [x] 部署运维：Docker + Kubernetes + Jenkins/GitHub Actions

### 业务需求（已完整确认 ✅）
- [x] 8大核心功能模块
- [x] 3大高并发场景（秒杀/支付/搜索）
- [x] 性能指标（QPS 5000+, P99<500ms）
- [x] 安全指标（加密、限流、审计）
- [x] 可用性指标（99.9%）

### 系统架构（已完整确认 ✅）
- [x] 4层架构设计
- [x] 9个微服务拆分
- [x] 数据库设计规范
- [x] 分库分表策略
- [x] 分布式事务方案
- [x] Kubernetes部署方案
- [x] 监控告警体系

---

## 🎯 下一步行动计划

### 阶段1：基础框架搭建（1-2周）
- [ ] 创建Maven父工程，配置依赖版本管理
- [ ] 搭建公共模块（common-core/redis/security/mq）
- [ ] 配置Spring Cloud Gateway网关
- [ ] 集成Nacos注册中心与配置中心
- [ ] 搭建统一异常处理与日志体系

### 阶段2：核心服务开发（4-6周）
- [ ] 用户服务（注册/登录/权限/会员）
- [ ] 商品服务（CRUD/分类/库存查询）
- [ ] 订单服务（购物车/下单/状态流转）
- [ ] 支付服务（支付宝/微信对接）
- [ ] 营销服务（秒杀/优惠券）

### 阶段3：高级特性（3-4周）
- [ ] Seata分布式事务集成
- [ ] 秒杀限流熔断实现
- [ ] 多级缓存架构优化
- [ ] SkyWalking链路追踪
- [ ] JMeter性能压测优化

### 阶段4：前端开发（3-4周）
- [ ] 用户商城UI（Vue3 + Naive UI）
- [ ] 管理后台UI（数据看板 + ECharts）
- [ ] 移动端H5适配

### 阶段5：部署上线（1-2周）
- [ ] 编写Kubernetes部署文件
- [ ] 配置CI/CD流水线（GitHub Actions）
- [ ] 集成Prometheus + Grafana监控
- [ ] 配置告警规则（钉钉/企业微信）
- [ ] 安全加固验证

---

## 📊 项目亮点总结

1. **命名高级**：StratosCommerce（平流层商业），简洁有力，国际化友好
2. **技术主流**：Spring Boot 3.2 + Vue 3 + K8s，行业标准技术栈
3. **架构完整**：9个微服务清晰拆分，职责明确
4. **安全加固**：OAuth 2.0 + 数据加密 + WAF + 审计日志
5. **前端精美**：Naive UI + Tailwind + 深色主题，设计感强
6. **场景完整**：秒杀、支付、搜索三大高并发场景全覆盖
7. **可落地性**：详细的技术选型、架构设计、部署方案

---

## 📞 项目信息

- **项目名称**：StratosCommerce
- **中文名称**：StratosCommerce 分布式商业交易平台
- **GitHub仓库**：stratos-commerce
- **技术栈**：Spring Boot 3.2 + Spring Cloud Alibaba + Vue 3 + MySQL + Redis
- **目标用户**：中大型企业、B2C/B2B电商平台
- **部署模式**：私有化部署 / 云原生Kubernetes
- **开源协议**：Apache License 2.0

---

<div align="center">

**📌 所有技术栈、业务需求、系统架构已全部确认完毕 ✅**

**准备就绪，可以开始项目开发！**

</div>
