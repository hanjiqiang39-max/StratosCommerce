# k6 冒烟压测

本机先起网关（8080）及商品 / 用户 / 订单 / 营销等服务。

```bash
# 安装 k6 后
k6 run docs/loadtest/k6-smoke.js

# 指定网关
k6 run -e BASE_URL=http://localhost:8080 docs/loadtest/k6-smoke.js
```

脚本覆盖：

- 商品列表浏览
- 注册 / 登录 / 下单（setup 阶段）
- 秒杀接口恒定到达率（约 20 QPS，可改 `options.scenarios.seckill.rate`）
