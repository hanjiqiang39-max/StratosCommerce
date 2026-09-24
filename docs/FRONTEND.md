# 前端本地与上线

## 开发

先启动网关 `8080` 和相关微服务。

```bash
cd stratos-shop-web
npm install
npm run dev
# http://localhost:5173

cd stratos-admin-web
npm install
npm run dev
# http://localhost:5174

cd stratos-merchant-web
npm install
npm run dev
# http://localhost:5175
```

开发环境通过 Vite 把 `/api` 代理到 `http://localhost:8080`。

## WebStorm

用 WebStorm **打开整个 `E:\StratosCommerce` 目录**（和 IDEA 看后端同一个仓库）。

1. `File` → `Settings` → `Languages & Frameworks` → `Node.js`：Node interpreter 选本机 Node（一般是 `node.exe`）。
2. 装 Vue 插件：`Settings` → `Plugins` 搜索 **Vue.js**（WebStorm 通常自带）。
3. 右上角运行配置选：
  - `Shop: dev` → 商城 http://localhost:5173
  - `Admin: dev` → 后台 http://localhost:5174
  - `Merchant: dev` → 商家端 http://localhost:5175
  - `Start Frontend` → 三个一起启动
4. 也可以在 `package.json` 左侧绿三角点 `shop` / `admin` / `merchant`。

代码在：

- `stratos-shop-web/src` 商城
- `stratos-admin-web/src` 后台
- `stratos-merchant-web/src` 商家端

- 商城登录：买家账号（先注册）
- 后台登录：`admin` / `admin123`
- 商家登录：`merchant` / `merchant123`（新商家先入驻，后台「商家入驻」审核）

## 构建

```bash
cd stratos-shop-web && npm run build
cd stratos-admin-web && npm run build
cd stratos-merchant-web && npm run build
```

产物在各自 `dist/`。生产环境 `VITE_API_BASE=/api`。

## Nginx

参考 [deploy/nginx/stratos-web.conf](../deploy/nginx/stratos-web.conf)：

- `shop.example.com` → shop `dist`
- `admin.example.com` → admin `dist`
- `merchant.example.com` → merchant `dist`
- `/api/` 反代网关 8080
