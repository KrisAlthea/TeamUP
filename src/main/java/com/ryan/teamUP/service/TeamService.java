package com.ryan.teamUP.service;

import com.ryan.teamUP.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ryan.teamUP.model.domain.User;

/**
* @author Haoran
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-04-01 14:26:05
*/
public interface TeamService extends IService<Team> {
	/**
	 *   添加队伍
	 * @param team
	 * @param loginUser
	 * @return
	 */
	long addTeam(Team team, User loginUser);
}
