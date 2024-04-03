package com.ryan.teamUP.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ryan.teamUP.common.BaseResponse;
import com.ryan.teamUP.common.ErrorCode;
import com.ryan.teamUP.common.ResultUtils;
import com.ryan.teamUP.exception.BusinessException;
import com.ryan.teamUP.model.domain.Team;
import com.ryan.teamUP.model.domain.User;
import com.ryan.teamUP.model.domain.UserTeam;
import com.ryan.teamUP.model.request.TeamAddRequest;
import com.ryan.teamUP.model.dto.TeamQuery;
import com.ryan.teamUP.model.request.TeamJoinRequest;
import com.ryan.teamUP.model.request.TeamQuitRequest;
import com.ryan.teamUP.model.request.TeamUpdateRequest;
import com.ryan.teamUP.model.vo.TeamUserVO;
import com.ryan.teamUP.service.TeamService;
import com.ryan.teamUP.service.UserService;
import com.ryan.teamUP.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description 队伍相关操作
 * @createDate 2024-04-01 14:24:44
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

	@Resource
	private UserService userService;

	@Resource
	private TeamService teamService;

	@Resource
	private UserTeamService userTeamService;

	@PostMapping("/add")
	public BaseResponse<Long> addTeam (@RequestBody Team team) {
		if (team == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		boolean save = teamService.save(team);
		if (!save) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入失败");
		}
		return ResultUtils.success(team.getId());
	}

	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteTeam (@RequestBody long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		boolean result = teamService.removeById(id);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
		}
		return ResultUtils.success(true);
	}

	@PostMapping("/update")
	public BaseResponse<Boolean> updateTeam (@RequestBody TeamUpdateRequest team, HttpServletRequest request) {
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User logininUser = userService.getLoginUser(request);
		boolean result = teamService.updateTeam(team, logininUser);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
		}
		return ResultUtils.success(true);
	}

	@GetMapping("/get")
	public BaseResponse<Team> getTeamById (long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = teamService.getById(id);
		if (team == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		return ResultUtils.success(team);
	}

	@PostMapping("/add")
	public BaseResponse<Long> addTeam (@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
		if (teamAddRequest == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		User logininUser = userService.getLoginUser(request);
		Team team = new Team();
		BeanUtils.copyProperties(teamAddRequest, team);
		long teamId = teamService.addTeam(team, logininUser);
		return ResultUtils.success(teamId);
	}

	@GetMapping("/list")
	public BaseResponse<List<TeamUserVO>> listTeams (TeamQuery teamQuery, HttpServletRequest request) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean notAdmin = userService.notAdmin(request);
		// 1、查询队伍列表
		List<TeamUserVO> teamList = teamService.listTeams(teamQuery, notAdmin);
		final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
		// 2、判断当前用户是否已加入队伍
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		try {
			User loginUser = userService.getLoginUser(request);
			userTeamQueryWrapper.eq("userId", loginUser.getId());
			userTeamQueryWrapper.in("teamId", teamIdList);
			List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
			// 已加入的队伍 id 集合
			Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
			teamList.forEach(team -> {
				boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
				team.setHasJoin(hasJoin);
			});
		} catch (Exception ignored) {
		}
		// 3、查询已加入队伍的人数
		QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
		userTeamJoinQueryWrapper.in("teamId", teamIdList);
		List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
		// 队伍 id => 加入这个队伍的用户列表
		Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
		teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
		return ResultUtils.success(teamList);
	}

	@GetMapping("/list/page")
	public BaseResponse<Page<Team>> listPageTeams (TeamQuery teamQuery) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = new Team();
		BeanUtils.copyProperties(teamQuery, team);
		Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
		Page<Team> resultPage = teamService.page(page, queryWrapper);
		return ResultUtils.success(resultPage);
	}

	@PostMapping("/join")
	public BaseResponse<Boolean> joinTeam (@RequestBody TeamJoinRequest team, HttpServletRequest request) {
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.joinTeam(team, loginUser);
		return ResultUtils.success(result);
	}

	@PostMapping("/quit")
	public BaseResponse<Boolean> quitTeam (@RequestBody TeamQuitRequest team, HttpServletRequest request) {
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.quitTeam(team, loginUser);
		return ResultUtils.success(result);
	}

	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteTeam (@RequestBody long id, HttpServletRequest request) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.deleteTeam(id, loginUser);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
		}
		return ResultUtils.success(true);
	}

	/**
	 * 获取我创建的队伍
	 *
	 * @param teamQuery
	 * @param request
	 * @return
	 */
	@GetMapping("/list/my/create")
	public BaseResponse<List<TeamUserVO>> listMyCreateTeams (TeamQuery teamQuery, HttpServletRequest request) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User logininUser = userService.getLoginUser(request);
		boolean notAdmin = userService.notAdmin(request);
		teamQuery.setUserId(logininUser.getId());
		List<TeamUserVO> teamList = teamService.listTeams(teamQuery, notAdmin);
		return ResultUtils.success(teamList);
	}

	/**
	 * 获取我加入的队伍
	 *
	 * @param teamQuery
	 * @param request
	 * @return
	 */
	@GetMapping("/list/my/join")
	public BaseResponse<List<TeamUserVO>> listMyJoinTeams (TeamQuery teamQuery, HttpServletRequest request) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User logininUser = userService.getLoginUser(request);
		QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userId", logininUser.getId());
		List<UserTeam> userTeamlist = userTeamService.list(queryWrapper);
		// 取出不重复的队伍 id
		//teamId userId
		//1,2
		//1,3
		//2,3
		//result
		//1=> 2,3
		//2=> 3
		Map<Long, List<UserTeam>> listMap = userTeamlist.stream().
				collect(Collectors.groupingBy(UserTeam::getTeamId));
		ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
		teamQuery.setIdList(idList);
		List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
		return ResultUtils.success(teamList);
	}
}
