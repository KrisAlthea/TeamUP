package com.ryan.teamUP.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryan.teamUP.model.domain.Team;
import com.ryan.teamUP.service.TeamService;
import com.ryan.teamUP.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Haoran
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-04-01 14:26:05
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




