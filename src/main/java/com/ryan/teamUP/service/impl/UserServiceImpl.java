package com.ryan.teamUP.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryan.teamUP.common.ErrorCode;
import com.ryan.teamUP.exception.BusinessException;
import com.ryan.teamUP.service.UserService;
import com.ryan.teamUP.model.domain.User;
import com.ryan.teamUP.mapper.UserMapper;
import com.ryan.teamUP.utils.AlgorithmUtils;
import org.apache.commons.math3.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ryan.teamUP.constant.UserConstant.ADMIN_ROLE;
import static com.ryan.teamUP.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author Haoran
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-11-07 15:02:48
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
		implements UserService {

	@Resource
	private UserMapper userMapper;

	/**
	 * 加密盐
	 */
	private static final String SALT = "ryan";

	@Override
	public long userRegister (String userAccount, String userPassword, String checkPassword, String planetCode) {
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度不能小于4");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
		}
		if (planetCode.length() > 5) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号长度不能大于5");
		}
		//账户不能包含特殊字符
		String validPattern = "^[a-zA-Z0-9_]+$";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (!matcher.find()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
		}
		//两次密码不相同
		if (!StringUtils.equals(userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不相同");
		}
		//前面判断完后再查表，避免性能浪费
		//账户不能重复
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		long count = userMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能重复");
		}
		//星球编号不能重复
		queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("planetCode", planetCode);
		count = userMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号不能重复");
		}
		//加密密码
		String password = userPassword + SALT;
		String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
		//插入数据库
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(md5Password);
		user.setPlanetCode(planetCode);
		//防止装箱错误，long2Long
		boolean result = this.save(user);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
		}
		return user.getId();
	}

	@Override
	public User userLogin (String userAccount, String userPassword, HttpServletRequest request) {
		//非空
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
		}
		//账户不小于4
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度不能小于4");
		}
		//密码不小于8
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
		}
		//账户不能包含特殊字符
		String validPattern = "^[a-zA-Z0-9_]+$";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (!matcher.find()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
		}
		//加密密码
		String password = userPassword + SALT;
		String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
		//查询数据库
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", md5Password);
		User user = userMapper.selectOne(queryWrapper);
		//用户不存在
		if (user == null) {
			log.info("user login failed, userAccount: {}", userAccount);
			throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
		}
		//脱敏
		User safetyUser = getSafetyUser(user);
		//记录用户的登录态
		request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
		return safetyUser;
	}

	/**
	 * 脱敏
	 *
	 * @param originUser 原始用户
	 * @return 脱敏后的用户
	 */
	@Override
	public User getSafetyUser (User originUser) {
		if (originUser == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
		}
		User safetyUser = new User();
		safetyUser.setId(originUser.getId());
		safetyUser.setUsername(originUser.getUsername());
		safetyUser.setUserAccount(originUser.getUserAccount());
		safetyUser.setAvatarUrl(originUser.getAvatarUrl());
		safetyUser.setGender(originUser.getGender());
		safetyUser.setEmail(originUser.getEmail());
		safetyUser.setPhone(originUser.getPhone());
		safetyUser.setPlanetCode(originUser.getPlanetCode());
		safetyUser.setUserRole(originUser.getUserRole());
		safetyUser.setUserStatus(originUser.getUserStatus());
		safetyUser.setCreateTime(originUser.getCreateTime());
		safetyUser.setTags(originUser.getTags());
		safetyUser.setIsDelete(0);
		return safetyUser;
	}

	/**
	 * 用户退出登录
	 *
	 * @param request 请求
	 * @return 1
	 */
	@Override
	public int userLogout (HttpServletRequest request) {
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		throw new BusinessException(ErrorCode.SUCCESS, "退出登录成功");
	}

	/**
	 * 根据标签搜索用户。(内存过滤版)
	 *
	 * @param tagNameList 用户要搜索的标签
	 * @return 用户列表
	 */
	@Override
	public List<User> searchUsersByTags (List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//1.先查询所有用户
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		List<User> userList = userMapper.selectList(queryWrapper);
		Gson gson = new Gson();
		//2.判断内存中是否包含要求的标签 parallelStream()
		return userList.stream().filter(user -> {
			String tagstr = user.getTags();
//            if (StringUtils.isBlank(tagstr)){
//                return false;
//            }
			Set<String> tempTagNameSet = gson.fromJson(tagstr, new TypeToken<Set<String>>() {
			}.getType());
			//java8  Optional 来判断空
			tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());

			for (String tagName : tagNameList) {
				if (!tempTagNameSet.contains(tagName)) {
					return false;
				}
			}
			return true;
		}).map(this::getSafetyUser).collect(Collectors.toList());
	}

	/**
	 * @param request
	 * @return
	 */
	@Override
	public User getLoginUser (HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		if (userObj == null) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}
		return (User) userObj;
	}

	/**
	 * @param user
	 * @param loginUser
	 * @return
	 */
	@Override
	public int updateUser (User user, User loginUser) {
		long userId = user.getId();
		if (userId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//如果是管理员，允许更新任意用户
		//如果不是管理员，只允许更新自己的信息
		if (notAdmin(loginUser) && userId != loginUser.getId()) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}
		User oldUser = userMapper.selectById(userId);
		if (oldUser == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		return userMapper.updateById(user);
	}

	/**
	 * 是否为管理员
	 *
	 * @param request
	 * @return 是否为管理员
	 */
	public boolean notAdmin (HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
		return user == null || user.getUserRole() != ADMIN_ROLE;
	}

	/**
	 * 是否为管理员
	 *
	 * @param loginUser
	 * @return 是否为管理员
	 */
	@Override
	public boolean notAdmin (User loginUser) {
		return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
	}

	/**
	 * 推荐匹配用户
	 *
	 * @param num
	 * @param loginUser
	 * @return
	 */
	@Override
	public List<User> matchUsers (long num, User loginUser) {
//        这里我因为电脑内存问题，没有办法像鱼皮电脑那样可以存放100万数据，可以直接运行。所以我选择了运行5万条数据。
//        不然的话会报 OOM（内存）的问题
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.last("limit 50000");
//        List<User> userList = this.list(queryWrapper);

//         或者用page分页查询，自己输入或默认数值，但这样匹配就有限制了
//        List<User> userList = this.page(new Page<>(pageNum,pageSize),queryWrapper);

		QueryWrapper<User> queryWrapper = getUserQueryWrapper();
		List<User> userList = this.list(queryWrapper);

		String tags = loginUser.getTags();
		Gson gson = new Gson();
		List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
		}.getType());
		// 用户列表的下表 => 相似度'
		List<Pair<User, Long>> list = new ArrayList<>();
		// 依次计算当前用户和所有用户的相似度
		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			String userTags = user.getTags();
			//无标签的 或当前用户为自己
			if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
				continue;
			}
			List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
			}.getType());
			//计算分数
			long distance = AlgorithmUtils.minDistance(tagList, userTagList);
			list.add(new Pair<>(user, distance));
		}
		//按编辑距离有小到大排序
		List<Pair<User, Long>> topUserPairList = list.stream()
				.sorted((a, b) -> (int) (a.getValue() - b.getValue()))
				.limit(num)
				.collect(Collectors.toList());
		//有顺序的userID列表
		List<Long> userListVo = topUserPairList.stream().map(pari -> pari.getKey().getId()).collect(Collectors.toList());

		//根据id查询user完整信息
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.in("id", userListVo);
		Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
				.map(user -> getSafetyUser(user))
				.collect(Collectors.groupingBy(User::getId));

		// 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
		List<User> finalUserList = new ArrayList<>();
		for (Long userId : userListVo) {
			finalUserList.add(userIdUserListMap.get(userId).get(0));
		}
		return finalUserList;
	}

	private static QueryWrapper<User> getUserQueryWrapper () {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.isNotNull("tags");
		queryWrapper.select("id", "tags");
		return queryWrapper;
	}


	/**
	 * 根据标签搜索用户。(sql查询版)
	 *
	 * @param tagNameList 用户要搜索的标签
	 * @return 用户列表
	 * @Deprecated 过时
	 */
	@Deprecated
	private List<User> searchUsersByTagBySQL (List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		//拼接tag
		// like '%Java%' and like '%Python%'
		for (String tagList : tagNameList) {
			queryWrapper = queryWrapper.like("tags", tagList);
		}
		List<User> userList = userMapper.selectList(queryWrapper);
		return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
	}

}