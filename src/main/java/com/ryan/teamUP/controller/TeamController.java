package com.ryan.teamUP.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ryan.teamUP.common.BaseResponse;
import com.ryan.teamUP.common.ErrorCode;
import com.ryan.teamUP.common.ResultUtils;
import com.ryan.teamUP.exception.BusinessException;
import com.ryan.teamUP.model.domain.Team;
import com.ryan.teamUP.model.domain.User;
import com.ryan.teamUP.model.dto.TeamAddRequest;
import com.ryan.teamUP.model.dto.TeamQuery;
import com.ryan.teamUP.service.TeamService;
import com.ryan.teamUP.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description 队伍相关操作
 * @createDate 2024-04-01 14:24:44
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/"})
@Slf4j
public class TeamController {

	@Resource
	private UserService userService;

	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private TeamService teamService;

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
	public BaseResponse<Boolean> updateTeam (@RequestBody Team team) {
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = teamService.updateById(team);
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
		User logininUser = userService.getLogininUser(request);
		Team team = new Team();
		BeanUtils.copyProperties(teamAddRequest, team);
		long teamId = teamService.addTeam(team, logininUser);
		return ResultUtils.success(teamId);
	}

	@GetMapping("/list")
	public BaseResponse<List<Team>> listTeams (TeamQuery teamQuery) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = new Team();
		BeanUtils.copyProperties(team, teamQuery);
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
		List<Team> teamList = teamService.list(queryWrapper);
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
}
