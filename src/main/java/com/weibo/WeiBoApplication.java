package com.weibo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.weibo.service.PeopleSearch;
import com.weibo.service.ProxyCrawler;
import com.weibo.service.WeiboSearch;

@SpringBootApplication
public class WeiBoApplication implements CommandLineRunner{
    @Autowired
    WeiboSearch crawler;
    @Autowired
    PeopleSearch peopleCrawler;
    @Autowired
    ProxyCrawler proxyCrawler;
	public static void main(String[] args) {
	    SpringApplication.run(WeiBoApplication.class, args);
	}
	@Override
	public void run(String... args) {

	    Thread t1 = new Thread(crawler);
	    t1.start();
//	    Thread t2 = new Thread(peopleCrawler);
//	    t2.start();
//	    Thread t3 = new Thread(proxyCrawler);
//	    t3.start();
	}

}
