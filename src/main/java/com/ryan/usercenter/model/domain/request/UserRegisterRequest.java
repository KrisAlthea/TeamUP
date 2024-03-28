package com.ryan.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @Author Haoran
 */
@Data
public class UserRegisterRequest implements Serializable {

	private static final long serialVersionUID = 1407225419991674020L;

	private String userAccount;

	private String userPassword;

	private String checkPassword;

	private String planetCode;
}
