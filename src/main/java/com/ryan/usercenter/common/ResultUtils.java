package com.ryan.usercenter.common;

/**
 * 返回工具类
 *
 * @Author Haoran
 */
public class ResultUtils {

	/**
	 * 成功返回
	 *
	 * @param data 返回数据
	 * @param <T>  返回数据类型
	 * @return BaseResponse
	 */
	public static <T> BaseResponse<T> success (T data) {
		return new BaseResponse<>(0, data, "ok");
	}

	/**
	 * 失败返回
	 *
	 * @param errorCode 错误码
	 * @return BaseResponse
	 */
	public static <T> BaseResponse<T> error (ErrorCode errorCode) {
		return new BaseResponse<>(errorCode);
	}

	/**
	 * 失败返回
	 *
	 * @param errorCode 错误码
	 * @param message  错误信息
	 * @param description 错误描述
	 * @return BaseResponse
	 */
	public static BaseResponse error (ErrorCode errorCode, String message, String description) {
		return new BaseResponse<>(errorCode.getCode(), null, message, description);
	}

	/**
	 * 失败返回
	 *
	 * @param errorCode 错误码
	 * @param description 错误描述
	 * @return BaseResponse
	 */
	public static BaseResponse error (ErrorCode errorCode, String description) {
		return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), description);
	}

	/**
	 * 失败返回
	 *
	 * @param code 错误码
	 * @param message  错误信息
	 * @param description 错误描述
	 * @return BaseResponse
	 */
	public static BaseResponse error (int code, String message, String description) {
		return new BaseResponse<>(code, null, message, description);
	}
}
