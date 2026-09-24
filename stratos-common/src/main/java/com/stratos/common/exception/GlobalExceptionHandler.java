package com.stratos.common.exception;

import com.stratos.common.result.Result;
import com.stratos.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.failed(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@RequestBody + @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常: {}", message);
        return Result.failed(ResultCode.PARAM_VALIDATE_ERROR.getCode(), message);
    }

    /**
     * 参数绑定异常（表单提交）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常: {}", message);
        return Result.failed(ResultCode.PARAM_VALIDATE_ERROR.getCode(), message);
    }

    /**
     * 参数校验异常（@RequestParam + @Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常: {}", message);
        return Result.failed(ResultCode.PARAM_VALIDATE_ERROR.getCode(), message);
    }

    /**
     * 浏览器访问 /、favicon.ico 等静态路径时不要记成系统异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("静态资源不存在: {}", e.getResourcePath());
        return Result.failed(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        String detail = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return Result.failed(ResultCode.INTERNAL_SERVER_ERROR.getCode(),
                ResultCode.INTERNAL_SERVER_ERROR.getMessage() + ": " + detail);
    }

}
