package com.ryan.teamUP.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: WebMVCConfig
 * Package: com.ryan.teamUP.config
 * Description:
 *
 * @Author Haoran
 * @Create 3/29/2024 1:08 PM
 * @Version 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		//设置允许跨域的路径
		registry.addMapping("/**")
				//设置允许跨域请求的域名
				//当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
				/* 放自己的前端域名*/
				.allowedOrigins("http://localhost:5173", "http://127.0.0.1:8083")
				//是否允许证书 不再默认开启
				.allowCredentials(true)
				//设置允许的方法
				.allowedMethods("*")
				//跨域允许时间
				.maxAge(3600);
	}
}
