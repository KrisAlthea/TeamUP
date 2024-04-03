package com.ryan.teamUP.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: TeamQuitRequest
 * Package: com.ryan.teamUP.model.request
 * Description:
 *
 * @Author Haoran
 * @Create 4/3/2024 11:06 AM
 * @Version 1.0
 */
@Data
public class TeamQuitRequest implements Serializable {

	private static final long serialVersionUID = 3191241716373120793L;

	/**
	 * 队伍id
	 */
	private Long teamId;
}
