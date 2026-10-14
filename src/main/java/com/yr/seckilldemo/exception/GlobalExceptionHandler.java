package com.yr.seckilldemo.exception;

import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        log.error("Request failed", e);

        if (e instanceof GlobalException globalException) {
            return Result.error(globalException.getResultEnum());
        }
        if (e instanceof BindException bindException) {
            Result result = Result.error(ResultEnum.BIND_ERROR);
            result.setMsg(bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return result;
        }
        return Result.error(ResultEnum.ERROR);
    }
}
