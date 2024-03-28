package com.ryan.teamUP;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ryan.teamUP.mapper")
public class teamUPApplication {

	public static void main(String[] args) {
		SpringApplication.run(teamUPApplication.class, args);
	}

}
