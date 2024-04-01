package com.ryan.teamUP.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ryan.teamUP.common.BaseResponse;
import com.ryan.teamUP.common.ErrorCode;
import com.ryan.teamUP.common.ResultUtils;
import com.ryan.teamUP.exception.BusinessException;
import com.ryan.teamUP.model.domain.User;
import com.ryan.teamUP.model.request.UserLoginRequest;
import com.ryan.teamUP.model.request.UserRegisterRequest;
import com.ryan.teamUP.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ryan.teamUP.constant.UserConstant.USER_LOGIN_STATE;

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
@Slf4j
public class UserController {
	@Resource
	private UserService userService;
	@Resource
	private RedisTemplate redisTemplate;

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
		if (userService.notAdmin(request)) {
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

	@GetMapping("/search/tags")
	public BaseResponse<List<User>> searchUsersByTags (@RequestParam(required = false) List<String> tagNameList){
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		List<User> users = userService.searchUsersByTags(tagNameList);
		List<User> list = users.stream().map(
				user -> userService.getSafetyUser(user)).collect(Collectors.toList());
		return ResultUtils.success(list);
	}

	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteUser (@RequestBody long id, HttpServletRequest request) {
		//仅管理员可删除
		if (userService.notAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}

		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//Mybatis-plus自动转为逻辑删除
		boolean b = userService.removeById(id);
		return ResultUtils.success(b);
	}

	@PostMapping("/update")
	public BaseResponse<Integer> updateUser(@RequestBody User user , HttpServletRequest request) {
		//验证参数是否为空
		if (user == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//鉴权
		User loginUser = userService.getLogininUser(request);
		int result = userService.updateUser(user,loginUser);
		return ResultUtils.success(result);
	}

	@GetMapping("/recommend")
	public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
		User logininUser = userService.getLogininUser(request);
		String redisKey = String.format("shayu:user:recommend:%s",logininUser.getId());
		ValueOperations valueOperations = redisTemplate.opsForValue();
		//如果有缓存，直接读取
		Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
		if (userPage != null){
			return ResultUtils.success(userPage);
		}
		//无缓存，查数据库
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
		//写缓存,10s过期
		try {
			valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
		} catch (Exception e){
			log.error("redis set key error",e);
		}
		return ResultUtils.success(userPage);
	}
}
