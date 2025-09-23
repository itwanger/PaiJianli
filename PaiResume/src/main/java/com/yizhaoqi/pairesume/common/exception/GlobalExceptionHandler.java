package com.yizhaoqi.pairesume.common.exception;

import com.yizhaoqi.pairesume.common.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义服务异常
     */
    @ExceptionHandler(ServiceException.class)
    public R<?> handleServiceException(ServiceException e) {
        log.error(e.getMessage(), e);
        if (e.getCode() != null) {
            // 使用 ServiceException 的 code 作为响应的 code
            return R.fail(e.getCode(), e.getMessage());
        }
        // 如果 ServiceException 没有 code，则使用默认的失败响应
        return R.fail(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return R.fail(400, message);
    }


    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleAllExceptions(Exception e) {
        log.error(e.getMessage(), e);
        return R.fail("服务器内部错误，请联系管理员");
    }
}
