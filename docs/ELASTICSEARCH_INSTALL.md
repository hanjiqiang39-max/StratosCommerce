# 本机安装 ElasticSearch 8（Windows）

搜索服务默认连 `http://localhost:9200`，**不必装 IK 分词**即可联调。

## 方式一：Docker（推荐）

已安装 Docker Desktop 时：

```bat
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" docker.elastic.co/elasticsearch/elasticsearch:8.15.3
```

浏览器打开 http://localhost:9200 看到 JSON 即成功。

## 方式二：Zip 安装

1. 打开 https://www.elastic.co/downloads/elasticsearch 下载 Windows zip（选 8.15.x）。
2. 解压到例如 `D:\elasticsearch-8.15.3`。
3. 编辑 `config\elasticsearch.yml`，加上：

```yaml
discovery.type: single-node
xpack.security.enabled: false
http.port: 9200
```

4. 双击 `bin\elasticsearch.bat`，窗口保持打开。
5. 访问 http://localhost:9200 。

内存不够时，在 `config\jvm.options` 把 `-Xms` / `-Xmx` 改成 `512m`。

## 启动搜索服务后导入商品

1. IDEA 启动 `stratos-search`（8093）。
2. 商品服务已启动时执行：

```bat
curl -X POST http://localhost:8080/api/search/import
```

3. 搜索：

```bat
curl "http://localhost:8080/api/search/products?keyword=演示"
```

联想：`GET /api/search/suggest?keyword=演示`  
热词：`GET /api/search/hot`

## 可选：IK 中文分词

联调不需要。以后要更好的中文分词，再装 [elasticsearch-analysis-ik](https://github.com/medcl/elasticsearch-analysis-ik) 与 ES 同版本的插件，并把 `ProductDocument` 的 analyzer 改成 `ik_max_word`。
