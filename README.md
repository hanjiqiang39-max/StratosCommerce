# StratosCommerce

<div align="center">

![StratosCommerce Logo](https://img.shields.io/badge/Stratos-Commerce-38BDF8?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJMMiA3TDEyIDEyTDIyIDdMMTIgMloiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGQ9Ik0yIDEyTDEyIDE3TDIyIDEyIiBzdHJva2U9IndoaXRlIiBzdHJva2Utd2lkdGg9IjIiLz4KPHBhdGggZD0iTTIgMTdMMTIgMjJMMjIgMTciIHN0cm9rZT0id2hpdGUiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4=)

**企业级分布式商业交易平台**

*High-Performance Distributed E-Commerce Platform*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023-blue.svg)](https://github.com/alibaba/spring-cloud-alibaba)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)

[功能特性](#-核心功能) · [技术架构](#-技术架构) · [快速开始](#-快速开始) · [文档](#-文档)

</div>

---

## 📖 项目简介

**StratosCommerce**（平流层商业）是一个面向中大型企业的**高并发分布式电商交易平台**，支持私有化部署。

### 核心优势

| 特性 | 说明 |
|------|------|
| 🚀 **高性能** | 支持10万+并发用户，P99响应时间<500ms |
| 🔒 **强安全** | OAuth 2.0 + JWT认证，数据全链路加密，WAF防护 |
| 💎 **优美界面** | Vue3 + Naive UI + Tailwind CSS，深色主题科技感设计 |
| 🎯 **业务完整** | 覆盖秒杀、支付、物流、营销全链路业务 |
| 🛠️ **易扩展** | 微服务架构，服务可独立部署与扩展 |
| ☁️ **云原生** | Kubernetes部署，支持弹性伸缩与灰度发布 |

---

## ✨ 核心功能

### 用户端功能
- ✅ 用户注册登录（手机/邮箱/第三方）
- ✅ 商品浏览与搜索（ES全文检索）
- ✅ 购物车与订单管理
- ✅ 多种支付方式（支付宝/微信/银联）
- ✅ 限时秒杀抢购
- ✅ 优惠券与满减活动
- ✅ 物流跟踪与售后服务
- ✅ 会员积分体系

### 商家端功能
- ✅ 商品发布与管理（SPU/SKU）
- ✅ 订单处理与发货
- ✅ 数据统计与报表
- ✅ 营销活动配置

### 管理端功能
- ✅ 系统配置管理
- ✅ 用户与权限管理
- ✅ 内容管理（轮播图/公告）
- ✅ 实时监控看板

---

## 🏗️ 技术架构

### 系统架构图
```
┌────────────────────────────────────────────────┐
│              接入层 (Gateway Layer)              │
│     Nginx + Spring Cloud Gateway + Sentinel     │
└────────────┬───────────────────────────────────┘
             │
    ┌────────┼────────┐
    ▼        ▼        ▼
┌─────────────────────────────────────────────────┐
│           业务服务层 (Service Layer)              │
│  用户 │ 商品 │ 订单 │ 支付 │ 营销 │ 库存 │ 搜索   │
└────────────┬────────────────────────────────────┘
             │
    ┌────────┼────────┐
    ▼        ▼        ▼
┌─────────────────────────────────────────────────┐
│          中间件层 (Middleware Layer)              │
│  Nacos │ Seata │ RocketMQ │ SkyWalking │ MinIO  │
└────────────┬────────────────────────────────────┘
             │
    ┌────────┼────────┐
    ▼        ▼        ▼
┌─────────────────────────────────────────────────┐
│             数据层 (Data Layer)                  │
│  MySQL │ Redis │ ElasticSearch │ MongoDB        │
└─────────────────────────────────────────────────┘
```

### 技术栈清单

#### 后端技术
- **核心框架**: Spring Boot 3.2, Spring Cloud Alibaba 2023
- **服务治理**: Nacos, Sentinel, Seata
- **数据访问**: MyBatis-Plus, ShardingSphere-JDBC
- **安全认证**: Spring Security, OAuth 2.0, JWT
- **消息队列**: RocketMQ 5.x
- **任务调度**: XXL-Job
- **链路追踪**: SkyWalking
- **监控告警**: Prometheus + Grafana

#### 前端技术
- **框架**: Vue 3.4 + TypeScript 5.4 + Vite 5
- **UI组件**: Naive UI + Tailwind CSS
- **状态管理**: Pinia
- **动画库**: GSAP
- **可视化**: ECharts

#### 数据存储
- **关系数据库**: MySQL 8.0 (主从复制 + 分库分表)
- **缓存**: Redis 7.x (哨兵模式)
- **搜索引擎**: ElasticSearch 8.x
- **文档数据库**: MongoDB 7.x
- **对象存储**: MinIO

#### 部署运维
- **容器化**: Docker + Docker Compose
- **编排**: Kubernetes
- **CI/CD**: GitHub Actions / Jenkins
- **反向代理**: Nginx
- **日志**: ELK Stack

完整技术栈详见 → [TECH_STACK.md](TECH_STACK.md)

---

## 🚀 快速开始

### 环境要求
```
- Java 21+
- Node.js 20+
- Docker 24+
- MySQL 8.0+
- Redis 7.0+
```

### 本地开发

#### 1. 克隆项目
```bash
git clone https://github.com/hanjiqiang39-max/StratosCommerce.git
cd stratos-commerce
```

#### 2. 启动基础设施（Docker Compose）
```bash
cd docker
docker-compose up -d
```

包含服务：
- MySQL (3306)
- Redis (6379)
- Nacos (8848)
- RocketMQ (9876, 10911)
- ElasticSearch (9200)
- Kibana (5601)

本机未用 Docker 时，ES 安装见 [docs/ELASTICSEARCH_INSTALL.md](docs/ELASTICSEARCH_INSTALL.md)。

#### 3. 启动后端服务
```bash
# 网关
cd stratos-gateway
mvn spring-boot:run

# 用户服务
cd stratos-user-service
mvn spring-boot:run

# 其他服务同理...
```

#### 4. 启动前端
```bash
# 用户商城
cd stratos-shop-web
npm install
npm run dev

# 管理后台
cd stratos-admin-web
npm install
npm run dev

# 商家端
cd stratos-merchant-web
npm install
npm run dev
```

#### 5. 访问应用
- 用户商城: http://localhost:5173
- 管理后台: http://localhost:5174
- 商家端: http://localhost:5175（`merchant` / `merchant123`）
- Nacos控制台: http://localhost:8848/nacos (nacos/nacos)

---

## 📚 文档

| 文档 | 说明 |
|------|------|
| [本机启动手册](docs/START.md) | 小皮 / DataGrip / Nacos / RocketMQ / IDEA / 前端 |
| [技术栈方案](TECH_STACK.md) | 完整技术选型与安全加固清单 |
| [业务需求文档](BUSINESS_REQUIREMENTS.md) | 功能清单与核心场景说明 |
| [系统架构设计](SYSTEM_ARCHITECTURE.md) | 微服务拆分与核心流程设计 |
| [数据库设计](docs/DATABASE_DESIGN.md) | 表结构与索引设计规范 |
| [接口文档](docs/API_DOCUMENTATION.md) | RESTful API接口定义 |
| [部署手册](docs/DEPLOYMENT_GUIDE.md) | Kubernetes部署指南 |
| [开发规范](docs/DEVELOPMENT_STANDARDS.md) | 代码规范与最佳实践 |

---

## 🎯 项目进度

### 第一阶段：基础框架搭建 ✅
- [x] 项目脚手架生成（Maven多模块）
- [x] 公共模块封装（Result、异常、工具类、配置）
- [x] Gateway网关配置
- [x] Nacos集成
- [x] 统一异常处理
- [x] 日志体系搭建
- [x] 数据库设计（10个业务库，75张表）
- [x] SQL初始化脚本（DDL + 种子数据）

### 第二阶段：核心服务开发 📅
- [x] 用户服务（注册/登录/JWT/地址/会员等级/短信验证码登录）
- [x] 商品服务（CRUD/分类树/品牌/搜索索引同步）
- [x] 订单服务（购物车/下单/取消/售后申请）
- [x] 支付服务（创单/支付宝沙箱/模拟支付/退款）
- [x] 营销服务（秒杀/优惠券领取与使用）
- [x] 搜索服务（ES 全文检索/联想/热词/商品导入）
- [x] 通知服务（站内信/短信通道/支付成功通知）
- [x] 物流运费试算 / 发货轨迹
- [x] 售后申请、查询与审核
- [x] 管理端登录、管理员列表

### 第三阶段：高级特性 📅
- [x] 分布式事务（Seata AT，订单/库存/支付）
- [x] 秒杀 Redis Lua 原子扣库存与限购
- [x] 秒杀/下单网关限流（Sentinel）
- [ ] 多级缓存架构
- [ ] 链路追踪
- [ ] 性能压测优化

### 第四阶段：前端开发 📅
- [ ] 用户商城UI
- [ ] 管理后台UI
- [ ] 移动端适配

### 第五阶段：部署上线 📅
- [ ] Kubernetes配置
- [ ] CI/CD流水线
- [ ] 监控告警
- [ ] 安全加固

---

## 🔒 安全特性

- ✅ **认证授权**: OAuth 2.0 + JWT无状态认证
- ✅ **密码加密**: Bcrypt强密码哈希
- ✅ **数据加密**: AES-256敏感数据加密
- ✅ **传输加密**: HTTPS/TLS 1.3
- ✅ **防注入**: 参数校验 + 参数化查询
- ✅ **防XSS**: HTML实体转义
- ✅ **防CSRF**: SameSite Cookie
- ✅ **限流防刷**: Sentinel接口限流
- ✅ **审计日志**: 操作留痕与溯源
- ✅ **WAF防护**: 阿里云WAF / Cloudflare

---

## 📊 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 并发用户数 | 10万+ | 高峰期同时在线用户 |
| QPS | 5000+ | 单服务每秒请求数 |
| 响应时间 | P99<500ms | 99%请求响应时间 |
| 系统可用性 | 99.9% | 年故障时间<8.76小时 |
| 数据可靠性 | 99.999% | 主从备份+定时备份 |

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下流程：

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

详见 [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 📄 开源协议

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

---

## 📮 联系我们

- 项目主页: https://github.com/hanjiqiang39-max/StratosCommerce
- 问题反馈: https://github.com/hanjiqiang39-max/StratosCommerce/issues
- 邮箱: contact@stratoscommerce.com

---

## ⭐ Star History

如果这个项目对你有帮助，请给我们一个⭐️！

[![Star History Chart](https://api.star-history.com/svg?repos=hanjiqiang39-max/StratosCommerce&type=Date)](https://star-history.com/#hanjiqiang39-max/StratosCommerce&Date)

---

<div align="center">

**Built with ❤️ by StratosCommerce Team**

</div>
