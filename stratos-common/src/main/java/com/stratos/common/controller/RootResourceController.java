package com.stratos.common.controller;

import com.stratos.common.result.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 避免浏览器打开端口根路径、自动拉 favicon 时打 ERROR 日志。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RootResourceController {

    @GetMapping("/")
    public Result<String> root() {
        return Result.success("ok");
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

}
