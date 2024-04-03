package com.ryan.teamUP.exception;

import com.ryan.teamUP.common.BaseResponse;
import com.ryan.teamUP.common.ErrorCode;
import com.ryan.teamUP.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @Author Haoran
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public BaseResponse businessExceptionHandler (BusinessException e) {
		log.error("businessException: " + e.getMessage(), e);
		return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
	}

	@ExceptionHandler(RuntimeException.class)
	public BaseResponse runtimeExceptionHandler (RuntimeException e) {
		log.error("runtimeException", e);
		return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
	}
}
