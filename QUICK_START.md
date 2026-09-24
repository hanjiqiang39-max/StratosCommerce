# StratosCommerce 快速开始

## 🚀 5分钟启动指南

### 前置条件
- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x
- Nacos 2.x

---

## 📦 安装步骤

### 1. 克隆项目
```bash
git clone https://github.com/hanjiqiang39-max/StratosCommerce.git
cd StratosCommerce
```

### 2. 初始化数据库
```bash
# 连接MySQL
mysql -u root -p

# 依次执行SQL脚本
source sql/00_init_databases.sql
source sql/01_stratos_user.sql
source sql/02_stratos_product.sql
source sql/03_stratos_order.sql
source sql/04_stratos_payment.sql
source sql/05_stratos_promotion.sql
source sql/06_stratos_inventory.sql
source sql/07_stratos_logistics.sql
source sql/08_stratos_message.sql
source sql/09_stratos_system.sql
source sql/10_stratos_notification.sql
source sql/99_seed_data.sql
```

### 3. 启动Nacos
```bash
# Linux/Mac
sh startup.sh -m standalone

# Windows
startup.cmd -m standalone
```

访问：http://localhost:8848/nacos （用户名/密码：nacos/nacos）

### 4. 启动Redis
```bash
redis-server
```

### 5. 修改配置文件

在每个服务的 `src/main/resources/application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stratos_xxx?useSSL=false
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
  
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

### 6. 编译项目
```bash
mvn clean package -DskipTests
```

### 7. 启动服务

**推荐启动顺序**：
```bash
# 1. 启动网关
cd stratos-gateway
mvn spring-boot:run

# 2. 启动认证中心
cd stratos-auth
mvn spring-boot:run

# 3. 启动业务服务（可并行）
cd stratos-user && mvn spring-boot:run
cd stratos-product && mvn spring-boot:run
cd stratos-order && mvn spring-boot:run
cd stratos-payment && mvn spring-boot:run
# ... 其他服务
```

---

## 🔍 验证安装

### 检查服务注册
访问 Nacos 控制台：http://localhost:8848/nacos

应该看到以下服务：
- `stratos-gateway`
- `stratos-auth`
- `stratos-user`
- `stratos-product`
- `stratos-order`
- ... 等

### 访问接口文档
各服务的 Swagger 文档地址：
- 网关：http://localhost:8080/doc.html
- 认证中心：http://localhost:8081/doc.html
- 用户服务：http://localhost:8082/doc.html
- 商品服务：http://localhost:8083/doc.html

对外统一走网关，前缀 `/api`（认证兼容 `/auth`）：
- 用户：`http://localhost:8080/api/user/**` → 用户服务 `/user/**`
- 订单：`http://localhost:8080/api/order/**` → 订单服务 `/order/**`
- 认证：`http://localhost:8080/auth/**` 或 `/api/auth/**`

### 测试登录接口
先启动 `stratos-user`（8082），再调认证中心或用户服务。密码需至少 8 位且含字母和数字：
```bash
curl -X POST http://localhost:8082/user/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"demo_user\",\"password\":\"Demo1234\",\"nickname\":\"演示用户\"}"

curl -X POST http://localhost:8082/user/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"demo_user\",\"password\":\"Demo1234\"}"

curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"demo_user\",\"password\":\"Demo1234\"}"
```

用户服务健康检查：
```bash
curl http://localhost:8080/api/user/health
curl http://localhost:8082/user/health
```

---

## 🎯 默认账号

### 管理后台
- 用户名：`admin`
- 密码：`admin123`

### Nacos控制台
- 用户名：`nacos`
- 密码：`nacos`

---

## 📝 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| stratos-gateway | 8080 | API网关 |
| stratos-auth | 8081 | 认证中心 |
| stratos-user | 8082 | 用户服务 |
| stratos-product | 8083 | 商品服务 |
| stratos-order | 8084 | 订单服务（连 `stratos_order_0`） |
| stratos-payment | 8085 | 支付服务 |
| stratos-promotion | 8086 | 营销服务 |
| stratos-inventory | 8087 | 库存服务 |
| stratos-logistics | 8088 | 物流服务 |
| stratos-message | 8089 | 消息服务 |
| stratos-system | 8090 | 系统管理 |
| stratos-search | 8091 | 搜索服务 |
| stratos-notification | 8092 | 通知服务 |

---

## ⚠️ 常见问题

### 1. 编译失败："无效的目标发行版: 17"
**原因**：JAVA_HOME指向JDK 8或更低版本

**解决**：
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. Nacos连接失败
**检查**：
- Nacos是否启动：`netstat -an | findstr 8848`
- 配置文件中 `server-addr` 是否正确

### 3. 数据库连接失败
**检查**：
- MySQL是否启动
- 数据库名称、用户名、密码是否正确
- 数据库是否已创建（执行 `00_init_databases.sql`）
- 订单服务连的是 `stratos_order_0`，不是 `stratos_order`

### 4. Redis连接失败
**检查**：
- Redis是否启动：`redis-cli ping`
- 密码配置是否匹配

---

## 📚 更多文档

- [架构设计](README.md#-技术架构)
- [开发进度](PROGRESS.md)
- [API文档](http://localhost:8080/doc.html)
- [贡献指南](CONTRIBUTING.md)

---

**祝您使用愉快！** 🎉
