# 本机启动手册（小皮 + DataGrip + IDEA + 前端）

每天开机按这个顺序即可：**小皮（MySQL / Redis）→ Nacos → RocketMQ → IDEA 后端 → 前端**。  
Seata 已经关掉，不必开。Elasticsearch 只影响搜索，不影响下单。

---

## 1. 每天必开的中间件

| 顺序 | 软件 | 端口 | 本机怎么开 | 不打开会怎样 |
|------|------|------|------------|--------------|
| 1 | MySQL 8 | `3306` | 小皮面板里启动 MySQL | 后端全部连不上库 |
| 2 | Redis | `6379`，无密码 | 小皮面板里启动 Redis | 登录、库存、秒杀会失败 |
| 3 | Nacos | `8848` | 见下面「Nacos」 | 服务注册失败，网关找不到后端 |
| 4 | RocketMQ NameServer | `9876` | 见下面「RocketMQ」 | 订单 / 库存 / 支付 / 通知可能起不来或报 MQ 错 |

当前项目约定账号：

- MySQL：`root` / `123456`
- Nacos 控制台：http://localhost:8848/nacos ，`nacos` / `nacos`

### 1.1 小皮

1. 打开小皮面板。
2. 启动 **MySQL**（8.x，端口 `3306`）。
3. 启动 **Redis**（端口 `6379`，不要设密码；项目配置是无密码）。
4. 小皮里的 Nginx / Apache **不用开**，前端用 Vite 自己的端口。

改过小皮 MySQL 密码的话，同步改各模块 `application.yml` 里的 `spring.datasource.password`。

### 1.2 Nacos（最容易忘）

用 **单机模式**。解压目录按你本机实际路径改，例如 `D:\nacos`。

```bat
cd /d D:\nacos\bin
startup.cmd -m standalone
```

- 窗口保持打开。关掉窗口 = Nacos 停了。
- 浏览器打开 http://localhost:8848/nacos ，账号密码都是 `nacos`。
- 「服务管理 → 服务列表」里，后面 IDEA 起来的服务会变成 UP。

停掉：同目录执行 `shutdown.cmd`，或直接关窗口。

如果提示端口被占用，说明上次没关干净，先结束占用 `8848` 的 Java 进程再开。

### 1.3 RocketMQ（第二容易忘）

需要两个窗口，都保持打开。解压目录按你本机改，例如 `D:\rocketmq`。

**窗口 1：NameServer**

```bat
cd /d D:\rocketmq\bin
mqnamesrv.cmd
```

看到 `The Name Server boot success` 再开第二个。

**窗口 2：Broker**

```bat
cd /d D:\rocketmq\bin
mqbroker.cmd -n 127.0.0.1:9876
```

看到 `The broker boot success` 即可。

项目里订单、库存、支付、通知连的是 `127.0.0.1:9876`。

### 1.4 不用开的

| 软件 | 原因 |
|------|------|
| Seata `:8091` | 订单 / 库存 / 支付里 `seata.enabled: false` |
| Elasticsearch `:9200` | 只给 `SearchApplication` 用，搜索挂了也能逛店下单 |
| Docker Compose | 你现在用小皮，不必再起一份 MySQL / Redis |

---

## 2. DataGrip 看库

1. 新建 MySQL 数据源：`localhost:3306`，用户 `root`，密码 `123456`。
2. 高级里字符集选 **utf8mb4**，避免中文变成问号。
3. 这个项目是「一个服务一个库」，不是单库多表。

| 库名 | 对应服务 | 备注 |
|------|----------|------|
| `stratos_user` | 用户 / 地址 | |
| `stratos_product` | 商品 / SKU | |
| `stratos_order_0`、`stratos_order_1` | 订单 | **没有**一张叫 `order_info` 的表，看 `order_info_0`～`order_info_7` |
| `stratos_payment` | 支付 | |
| `stratos_promotion` | 券 / 秒杀 / 拼团 / 满减 | |
| `stratos_inventory` | 锁库存 | 和商品库存是两套，下单走这里 |
| `stratos_logistics` | 运费 / 物流公司 | |
| `stratos_message` | 消息 | |
| `stratos_system` | 后台账号、轮播、商家 | |
| `stratos_notification` | 站内信 | |

### 第一次建库（空环境才整段跑）

在 DataGrip 里对 `root` 按顺序执行 `sql/`：

1. `00_init_databases.sql`
2. `01`～`10` 各业务库建表
3. `11_seata_undo_log.sql`、`12_promo_demo.sql`、`13_*.sql`、`14_merchant.sql`
4. `99_seed_data.sql`

已经跑过的库 **不要再整段执行 00～12**，那些脚本会 DROP，本地订单和商品会没。缺哪张表只跑对应的增量脚本。

---

## 3. IDEA 启动后端

1. 用 IDEA 打开 `E:\StratosCommerce`（Maven 多模块）。
2. JDK 选 **21**，Maven 能正常刷新。
3. 每个模块一个启动类，先 **Build** 再 **Run**。配置里 Active profile 用 `dev`（和现在的 `application.yml` 一致）。
4. **先开网关，再开业务。** 前端 `/api` 全部打到 `8080`。

建议顺序：

| 顺序 | 启动类 | 模块 | 端口 |
|------|--------|------|------|
| 1 | `GatewayApplication` | stratos-gateway | 8080 |
| 2 | `AuthApplication` | stratos-auth | 8081 |
| 3 | `UserApplication` | stratos-user | 8082 |
| 4 | `ProductApplication` | stratos-product | 8083 |
| 5 | `InventoryApplication` | stratos-inventory | 8087 |
| 6 | `OrderApplication` | stratos-order | 8084 |
| 7 | `PaymentApplication` | stratos-payment | 8085 |
| 8 | `PromotionApplication` | stratos-promotion | 8086 |
| 9 | `LogisticsApplication` | stratos-logistics | 8088 |
| 10 | `NotificationApplication` | stratos-notification | 8092 |
| 11 | `SystemApplication` | stratos-system | 8090 |

可选：`MessageApplication` 8089，`SearchApplication` 8093（要先有 ES）。

改了某个服务的 Java 代码：在 IDEA 里 **停掉再 Run**，只刷新前端不够。

本地密钥（支付宝、短信）放各模块的 `application-local.yml`，从同目录 `application-local.yml.example` 复制。这个文件已被 `.gitignore` 忽略，不要提交。

---

## 4. 前端（Web / 终端）

三个独立 Vite 项目，网关 `8080` 必须先起来。

```bat
cd /d E:\StratosCommerce\stratos-shop-web
npm install
npm run dev
```

```bat
cd /d E:\StratosCommerce\stratos-admin-web
npm install
npm run dev
```

```bat
cd /d E:\StratosCommerce\stratos-merchant-web
npm install
npm run dev
```

| 地址 | 端 | 演示账号 |
|------|----|----------|
| http://localhost:5173 | 商城 | 自己注册，密码至少 8 位且同时有字母和数字 |
| http://localhost:5174 | 后台 | `admin` / `admin123` |
| http://localhost:5175 | 商家 | `merchant` / `merchant123` |

浏览器开发者工具里 `/api` 应代理到 `http://localhost:8080`。

---

## 5. 起来之后先看这几个地址

| 地址 | 正常长什么样 |
|------|----------------|
| http://localhost:8848/nacos | 能登录，服务列表逐渐变 UP |
| http://localhost:8080 | 网关在听（接口走这里） |
| http://localhost:5173 | 商城首页 |
| DataGrip 能展开 `stratos_product` | 小皮 MySQL 没问题 |

主流程：商城注册 → 加购 → 填地址 → 结算 → 「完成本地支付」。商家端发货用 `merchant` / `merchant123`。

---

## 6. 常见没起来

| 现象 | 先查 |
|------|------|
| 前端所有接口失败 | 小皮 MySQL、Nacos、`GatewayApplication` 8080 |
| IDEA 里服务立刻退出 / 注册失败 | Nacos 没开，或不是 `startup.cmd -m standalone` |
| 报 Redis 连不上 | 小皮 Redis 没开 |
| 订单 / 库存启动报 RocketMQ | NameServer `9876` 和 Broker 两个窗口都要在 |
| Port already in use | 上次 Java 没停干净，在任务管理器结束占用端口的进程 |
| DataGrip 找不到 `order_info` | 去 `stratos_order_0` 看 `order_info_0`～`7` |
| 搜索不可用 | 正常，没开 ES / Search 服务 |

---

## 7. 准备上传 GitHub 开源

可以开源，但先做完下面几件事再 `git push`。需要我帮你建仓库并推上去时再说一声即可。

1. **不要提交密钥**  
   `.gitignore` 已经忽略 `application-local.yml`、`.env`。推之前执行 `git status`，确认没有支付宝私钥、短信 Key、本机密码文件。
2. **仓库设为 Public** 即可开源。协议是 Apache 2.0（根目录 `LICENSE`）。
3. **建议先在 GitHub 建空仓库**（不要勾选自动加 README，避免和本地冲突），再在项目根目录：

```bat
cd /d E:\StratosCommerce
git status
git remote add origin https://github.com/hanjiqiang39-max/StratosCommerce.git
git push -u origin HEAD
```

如果还没有 git 仓库，先 `git init`，再按常规加文件、提交、推送。

4. 开源说明里写清楚：本机默认 `root/123456`、`nacos/nacos`、`admin/admin123` 仅供演示，上线必须改掉。
