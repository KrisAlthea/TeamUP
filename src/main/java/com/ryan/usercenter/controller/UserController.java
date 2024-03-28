package com.ryan.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ryan.usercenter.common.BaseResponse;
import com.ryan.usercenter.common.ErrorCode;
import com.ryan.usercenter.common.ResultUtils;
import com.ryan.usercenter.exception.BusinessException;
import com.ryan.usercenter.model.domain.User;
import com.ryan.usercenter.model.domain.request.UserLoginRequest;
import com.ryan.usercenter.model.domain.request.UserRegisterRequest;
import com.ryan.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ryan.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.ryan.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户表 前端控制器
 *
 * @description 针对表【user(用户表)】的数据库操作Controller
 * @createDate 2023-11-07 15:02:48
 * @Author Haoran
 * @since 1.0
 * @deprecated
 */
@RestController
@RequestMapping("/user")
public class UserController {
	@Resource
	private UserService userService;

	@PostMapping("/register")
	public BaseResponse<Long> userRegister (@RequestBody UserRegisterRequest userRegisterRequest) {
		if (userRegisterRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		String planetCode = userRegisterRequest.getPlanetCode();
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		return ResultUtils.success(result);
	}

	@PostMapping("/login")
	public BaseResponse<User> userLogin (@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		if (userLoginRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.userLogin(userAccount, userPassword, request);
		return ResultUtils.success(user);
	}

	@PostMapping("/logout")
	public BaseResponse<Integer> userLogout (HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		return ResultUtils.success(userService.userLogout(request));
	}

	@GetMapping("/current")
	public BaseResponse<User> getCurrentUser (HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN);
		}
		long id = user.getId();
		User userFromDB = userService.getById(id);
		return ResultUtils.success(userService.getSafetyUser(userFromDB));
	}

	@GetMapping("/search")
	public BaseResponse<List<User>> searchUsers (String username, HttpServletRequest request) {
		//仅管理员可查询
		if (notAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}

		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		if (StringUtils.isNotBlank(username)) {
			queryWrapper.like("username", username);
		}
		List<User> users = userService.list(queryWrapper);
		List<User> list = users.stream().map(
				user -> userService.getSafetyUser(user)).collect(Collectors.toList());
		return ResultUtils.success(list);
	}

	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteUser (@RequestBody long id, HttpServletRequest request) {
		//仅管理员可删除
		if (notAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}

		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//Mybatis-plus自动转为逻辑删除
		boolean b = userService.removeById(id);
		return ResultUtils.success(b);
	}

	/**
	 * 是否是管理员
	 */
	private boolean notAdmin (HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
		return user == null || user.getUserRole() != ADMIN_ROLE;
	}
}
