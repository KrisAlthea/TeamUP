package com.ryan.teamUP.once;

import com.ryan.teamUP.mapper.UserMapper;
import com.ryan.teamUP.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * ClassName: InsertUsers
 * Package: com.ryan.teamUP.once
 * Description:
 *
 * @Author Haoran
 * @Create 4/1/2024 1:17 PM
 * @Version 1.0
 */
@Component
public class InsertUsers {
	@Resource
	private UserMapper userMapper;

	/**
	 * 循环插入用户
	 */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
	public void doInsertUser() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final int INSERT_NUM = 1000;
		for (int i = 0; i < INSERT_NUM; i++) {
			User user = new User();
			user.setUsername("占位用户");
			user.setUserAccount("fakeUser");
			user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
			user.setGender(0);
			user.setUserPassword("12345678");
			user.setPhone("123456789108");
			user.setEmail("xxxxxxxxxxx@qq.com");
			user.setUserStatus(0);
			user.setUserRole(0);
			user.setPlanetCode("12345");
			user.setTags("[]");
			userMapper.insert(user);
		}
		stopWatch.stop();
		System.out.println( stopWatch.getLastTaskTimeMillis());

	}
}
