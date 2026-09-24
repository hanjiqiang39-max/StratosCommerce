# Kubernetes 清单

不依赖现有集群。本机有 kubectl + kind/minikube 时可：

```bash
kubectl apply -k deploy/k8s
```

先把各模块 `mvn -pl <module> -am package` 后按模块打镜像，例如：

```bash
docker build -t stratos/stratos-gateway:1.0.0 stratos-gateway
```

`ConfigMap stratos-infra` 里改 Nacos / 数据源账号。网关 NodePort `30080`。
