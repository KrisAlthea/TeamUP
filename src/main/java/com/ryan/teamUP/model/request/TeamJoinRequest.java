package com.ryan.teamUP.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: TeamJoinRequest
 * Package: com.ryan.teamUP.model.request
 * Description:
 *
 * @Author Haoran
 * @Create 4/3/2024 10:58 AM
 * @Version 1.0
 */
@Data
public class TeamJoinRequest implements Serializable {

	private static final long serialVersionUID = 3191241716373120793L;

	/**
	 * 队伍id
	 */
	private Long teamId;

	/**
	 * psw
	 */
	private String password;
}
