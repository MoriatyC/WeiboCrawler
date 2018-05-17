package com.weibo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.weibo.service.WeiboSearch;

@SpringBootApplication
@ComponentScan(basePackages = {"com.weibo"})
public class WeiBoApplication implements CommandLineRunner{
    @Autowired
    WeiboSearch crawler;
	public static void main(String[] args) {
	    SpringApplication app = new SpringApplication(WeiBoApplication.class);
	    app.run(WeiBoApplication.class, args);
	}
	@Override
	public void run(String... args) {
	    if (crawler.peopleRepository == null) {
	        System.out.println(123);
	    } else {
	        System.out.println(312);
	    }
	    Thread t1 = new Thread(crawler);
	    t1.start();
	}
}
