package com.ryan.teamUP;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

/**
 * ClassName: RedisTest
 * Package: com.ryan.teamUP
 * Description:
 *
 * @Author Haoran
 * @Create 4/1/2024 12:01 PM
 * @Version 1.0
 */
public class RedisTest {
	public static void main(String[] args) {
		//参数：（服务器地址 : 端口号 / 仓库）
		JedisShardInfo shardInfo = new JedisShardInfo("redis://localhost:6379/0");
//		shardInfo.setPassword("");//这里是密码
		//连接redis数据库
		Jedis jedis = new Jedis(shardInfo);
		jedis.set("k1","v1");
		jedis.set("k2","v2");
		jedis.set("k3","v3");
		System.out.println(jedis.keys("*"));
	}
}
