package com.ryan.usercenter.exception;

import com.ryan.usercenter.common.BaseResponse;
import com.ryan.usercenter.common.ErrorCode;
import com.ryan.usercenter.common.ResultUtils;
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
	public BaseResponse businessExceptionHandler(BusinessException e){
		log.error("businessException: "+e.getMessage(), e);
		return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
	}

	@ExceptionHandler(RuntimeException.class)
	public BaseResponse runtimeExceptionHandler(RuntimeException e){
		log.error("runtimeException", e);
		return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
	}
}
