# 链路追踪（本机可落地）

各服务已引入 Micrometer Tracing（Brave）与 `X-Trace-Id`。日志 pattern 带 `traceId`。

```bash
# 看健康与 Prometheus 端点
curl http://localhost:8080/actuator/health
curl http://localhost:8084/actuator/prometheus
```

## 可选 SkyWalking

不强制本机装 OAP。若要接 SkyWalking：

1. 下载 `skywalking-agent`（与 Java 21 匹配的 9.x）
2. JVM 参数：

```
-javaagent:D:/skywalking-agent/skywalking-agent.jar
-Dskywalking.agent.service_name=stratos-order
-Dskywalking.collector.backend_service=127.0.0.1:11800
```

IDEA 各服务 Run Configuration 的 VM options 里加上即可。
