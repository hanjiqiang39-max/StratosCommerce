# 项目启动说明（按现在这套本机环境）

**日常开机请看 [START.md](./START.md)**（小皮 MySQL/Redis、Nacos、RocketMQ、IDEA、前端、DataGrip、GitHub）。  
下面是同一套环境的补充说明。今天的进度见 [TODAY_2026-09-23.md](./TODAY_2026-09-23.md)。

---

## 1. 你需要先准备什么

| 软件 | 说明 |
|------|------|
| JDK 21 | IDEA / WebStorm 运行后端 |
| Maven | 后端编译 |
| Node.js 20+ | 三个前端 |
| MySQL 8 | 密码按现在配置是 `123456`，用户 `root` |
| Redis | 本机 `6379`，无密码 |
| Nacos 2.3.2 | `localhost:8848`，账号 `nacos` / `nacos` |
| Seata 2.0 | `:8091`（订单服务目前没开 Seata，库存等服务可能仍连） |
| RocketMQ 5.x | NameServer `:9876` |
| Elasticsearch | **可选**，只影响搜索，不影响下单 |

支付宝沙箱密钥已经写在 `stratos-payment/src/main/resources/application-local.yml`（不要提交到 git）。

---

## 2. 数据库

- 第一次建库：按顺序执行 `sql/` 下的脚本（`00` 到 `12` 会 DROP，只在空库执行一次）。
- 已经跑过的库：**不要再整段重跑 00～12**，会清数据。
- 后来加的表（例如 `cms_banner`、商家表）用增量脚本，如 `sql/13_cms_banner.sql`、`sql/14_merchant.sql`。
- 订单在 `stratos_order_0` / `stratos_order_1`，表是 `order_info_0`～`order_info_7`，没有一张叫 `order_info` 的物理表。

---

## 3. 先开中间件，再开服务

建议顺序：

1. MySQL、Redis
2. Nacos
3. RocketMQ
4. Seata（可选，订单服务已关）
5. 后端微服务（IDEA 里 Run）
6. 前端（WebStorm 或 `npm run dev`）

### 后端在 IDEA 里启动

每个模块有一个 `*Application`，**先 Build 再 Run**。  
WebStorm 里 Java 运行配置红叉是正常的，用 IDEA 跑后端。

**建议启动顺序（主交易至少这些）：**

| 顺序 | 启动类 | 端口 | 干什么 |
|------|--------|------|--------|
| 1 | `GatewayApplication` | 8080 | 所有前端 `/api` 都打这里 |
| 2 | `AuthApplication` | 8081 | 登录注册 |
| 3 | `UserApplication` | 8082 | 用户、地址、积分 |
| 4 | `ProductApplication` | 8083 | 商品、分类 |
| 5 | `InventoryApplication` | 8087 | 锁库存 |
| 6 | `OrderApplication` | 8084 | 购物车、下单 |
| 7 | `PaymentApplication` | 8085 | 支付宝 / 模拟支付 |
| 8 | `PromotionApplication` | 8086 | 券、秒杀、拼团 |
| 9 | `LogisticsApplication` | 8088 | 运费、发货 |
| 10 | `NotificationApplication` | 8092 | 站内信 |
| 11 | `SystemApplication` | 8090 | 后台登录、轮播、公告 |

完整还可开：`MessageApplication` 8089、`SearchApplication` 8093。

改了 Java 代码后，对应服务要**停掉再 Run**（只刷新前端不够）。

### 前端

```bash
# 商城
cd stratos-shop-web
npm install
npm run dev
# http://localhost:5173

# 后台
cd stratos-admin-web
npm install
npm run dev
# http://localhost:5174

# 商家端
cd stratos-merchant-web
npm install
npm run dev
# http://localhost:5175
```

也可以用 WebStorm 打开整个 `E:\StratosCommerce`，运行配置 `Shop: dev` / `Admin: dev` / `Merchant: dev`。  
前端 `/api` 会代理到网关 `http://localhost:8080`，所以**网关必须先起来**。

---

## 4. 打开哪些地址

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | 商城 |
| http://localhost:5174 | 后台 |
| http://localhost:5175 | 商家端 |
| http://localhost:8080 | 网关（接口入口） |
| http://localhost:8848/nacos | Nacos，`nacos` / `nacos` |

**账号**

- 后台：`admin` / `admin123`
- 商家端：`merchant` / `merchant123`（官方旗舰店，承接已有演示商品）
- 商城：自己注册。密码规则：8～32 位，必须有字母也有数字，例如 `han12345`

---

## 5. 走一遍主流程（桌面）

1. 商城注册并登录  
2. 首页或分类里把演示手机加入购物车  
3. 先去「地址」加一条收货地址  
4. 购物车勾选 → 结算 → 提交订单  
5. 支付页：
   - **本地联调**：点「完成本地支付」，订单变已支付  
   - **看沙箱页**：点「打开支付宝沙箱」，用开放平台里的**沙箱买家账号**登录（不要扫正式支付宝）  
6. 沙箱收银台控制台里的 `unload` / Mixed Content 红字可以忽略  
7. 打开商家端 http://localhost:5175，用 `merchant` / `merchant123` 登录，在「订单」里发货  
8. 新商家走：商家端注册 → 后台「商家入驻」通过 → 商家上架商品 → 买家下单  

改过订单 / 库存 / 支付 / 商家代码后，至少重启：`GatewayApplication`、`SystemApplication`、`ProductApplication`、`OrderApplication`。先对现有库执行 `sql/14_merchant.sql`。

---

## 6. 常见「没起来」对照

| 现象 | 先看什么 |
|------|----------|
| 前端接口全失败 | 网关 8080 是否在跑；Nacos 里服务是否都 UP |
| 登录 / 注册 1001 | 密码是否同时有字母和数字；`UserApplication` 是否用最新代码重启过 |
| 结账 500 | `OrderApplication`、`InventoryApplication`；购物车有没有勾选；有没有地址 |
| 订单不存在 | 从「我的订单」点进去（带 `orderNo`），不要用手改过的超长数字 ID |
| 支付宝 404 | `PaymentApplication` 是否重启过（必须是 GET 跳转那版） |
| 沙箱付完订单还是待支付 | 正常。异步通知到不了 localhost，用「完成本地支付」 |
| DataGrip 没有 `order_info` | 去 `stratos_order_0` 看 `order_info_0`～`7` |
| 没有 `cms_banner` | 对 `stratos_system` 执行 `sql/13_cms_banner.sql` |
| 商家端登录失败 / 商品没有店铺 | 对现有库执行 `sql/14_merchant.sql`，并重启 System / Product / Order / Gateway |
| 搜索不可用 | Search 服务和 ES 没开，可先不管 |

---

## 7. 改代码时注意

- 支付宝密钥只放 `application-local.yml` 或环境变量，不要写进会提交的 `application.yml`。
- 订单表是分表，更新时不要 `SET user_id`。
- 前端不要对雪花 ID 做 `Number(...)`，用字符串或订单号。
- SQL 增量脚本加在 `sql/` 后面编号，不要改已经执行过的 DROP 脚本。

---

## 8. 今天先停在哪

主交易（注册 → 购物车 → 下单 → 本地支付）和后台基本页已经能用。  
明天优先做的事写在 [TODAY_2026-09-23.md](./TODAY_2026-09-23.md) 最后一节。
