package com.ryan.usercenter.service;

import com.ryan.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Haoran
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-11-07 15:02:48
 */
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 *
	 * @param userAccount   账号
	 * @param userPassword  密码
	 * @param checkPassword 确认密码
	 * @param planetCode    星球编号
	 * @return 新用户id
	 */
	long userRegister (String userAccount, String userPassword, String checkPassword, String planetCode);

	/**
	 * 用户登录
	 *
	 * @param userAccount  账号
	 * @param userPassword 密码
	 * @return 脱敏后的用户信息
	 */
	User userLogin (String userAccount, String userPassword, HttpServletRequest request);

	/**
	 * 用户脱敏
	 *
	 * @param originUser 原始用户信息
	 * @return 安全用户信息
	 */
	User getSafetyUser (User originUser);

	/**
	 * 用户退出登录
	 *
	 * @param request 请求
	 * @return 退出登录状态
	 */
	int userLogout (HttpServletRequest request);
}
