package com.weibo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.weibo.dao.RedisDao;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WeiBoApplicationTests {
    @Autowired
    RedisDao redis;
	@Test
	public void contextLoads() {
	    Map<String, String> proxy123 = new HashMap<>();
	    redis.hsetWithExpired("proxy:hash", proxy123);
	}

}
