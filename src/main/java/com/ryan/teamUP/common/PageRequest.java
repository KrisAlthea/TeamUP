package com.ryan.teamUP.common;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: PageRequest
 * Package: com.ryan.teamUP.common
 * Description:
 *
 * @Author Haoran
 * @Create 4/1/2024 2:36 PM
 * @Version 1.0
 */
@Data
public class PageRequest implements Serializable {
	private static final long serialVersionUID = 1407225419991674020L;

	protected int pageNum = 1;

	protected int pageSize = 10;
}
