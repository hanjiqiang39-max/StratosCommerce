package com.stratos.logistics.express;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratos.logistics.config.ExpressProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 快递100实时查询。密钥未配置时不会装配本实现。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class HttpExpressClient implements ExpressClient {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ExpressProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public List<ExpressTrace> query(String companyCode, String logisticsNo) {
        try {
            String param = "{\"com\":\"" + safe(companyCode) + "\",\"num\":\"" + safe(logisticsNo) + "\"}";
            String sign = DigestUtils.md5DigestAsHex(
                    (param + properties.getKey() + properties.getCustomer()).getBytes(StandardCharsets.UTF_8)
            ).toUpperCase();
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("customer", properties.getCustomer());
            form.add("sign", sign);
            form.add("param", param);
            String body = restClient.post()
                    .uri(properties.getQueryUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            return parse(body);
        } catch (Exception ex) {
            log.warn("快递100查询失败 no={}: {}", logisticsNo, ex.getMessage());
            return List.of();
        }
    }

    private List<ExpressTrace> parse(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = objectMapper.readTree(body);
        JsonNode data = root.path("data");
        List<ExpressTrace> traces = new ArrayList<>();
        if (!data.isArray()) {
            return traces;
        }
        for (JsonNode node : data) {
            ExpressTrace trace = new ExpressTrace();
            String timeText = node.path("ftime").asText(node.path("time").asText(""));
            if (!timeText.isBlank()) {
                try {
                    trace.setTime(LocalDateTime.parse(timeText, TIME_FMT));
                } catch (Exception ignored) {
                    trace.setTime(LocalDateTime.now());
                }
            } else {
                trace.setTime(LocalDateTime.now());
            }
            String status = node.path("status").asText("TRANSIT");
            trace.setStatus(status.isBlank() ? "TRANSIT" : status);
            trace.setDesc(node.path("context").asText(""));
            trace.setLocation(node.path("location").asText(""));
            traces.add(trace);
        }
        traces.sort((a, b) -> a.getTime().compareTo(b.getTime()));
        return traces;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "");
    }
}
