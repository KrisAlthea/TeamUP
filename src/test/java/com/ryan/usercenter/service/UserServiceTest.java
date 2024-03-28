package com.ryan.usercenter.service;
import java.util.Date;

import com.ryan.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户表 服务层测试
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-11-07 15:02:48
 * @Entity generator.domain.User
 * @since 1.0
 * @deprecated
 * @Author Haoran
 */
@SpringBootTest
class UserServiceTest {

	@Autowired
	private UserService userService;

	@Test
	void testAdd() {
		User user = new User();
		user.setUsername("ryan");
		user.setUserAccount("123");
		user.setAvatarUrl("dudo");
		user.setGender(0);
		user.setUserPassword("xxx");
		user.setEmail("123");
		user.setPhone("234");
		boolean result =  userService.save(user);
		System.out.println(user.getId());
		assertTrue(result);
	}

	@Test
	void userRegister () {
		//非空判断
		String userAccount = "ryan";
		String userPassword = "";
		String checkPassword = "12345678";
		String planetCode = "12345";
		long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertEquals(-1, result);
		//账户长度判断
		userAccount = "ry";
		result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertEquals(-1, result);
		//密码长度判断
		userAccount = "ryan";
		userPassword = "1234567";
		result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertEquals(-1, result);
		//账户不能包含特殊字符
		userAccount = "ry an";
		userPassword = "12345678";
		result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertEquals(-1, result);
		//两次密码不相同
		userAccount = "ryan";
		checkPassword = "12345679";
		result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertEquals(-1, result);
		//账户不能重复
		userAccount = "123";
		userPassword = "12345678";
		checkPassword = "12345678";
		result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertEquals(-1, result);
		//正常注册
		userAccount = "ryan9";
		userPassword = "12345678";
		checkPassword = "12345678";
		result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		assertNotEquals(-1, result);
	}
}