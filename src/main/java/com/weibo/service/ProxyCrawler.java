package com.weibo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.weibo.dao.RedisDao;
import com.weibo.utils.ConnectTest;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

/**
*@author: Menghui Chen
*@version: 2018年5月19日上午10:30:21
**/
@Service
public class ProxyCrawler implements PageProcessor, Runnable{
    @Autowired
    RedisDao redis;
    public static Map<String, String> proxy = new HashMap<>();
    Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
    static AtomicInteger index = new AtomicInteger(1);
    static HttpClientDownloader httpClientDownloader;
    static volatile boolean flag = true;
    public ProxyCrawler(String ip, int port) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy(ip, port)));
    }
    public ProxyCrawler() {
    }
    @Override
    public void process(Page page) {
        System.out.println(page.getUrl());
        
        if (flag) {
            int last = Integer.valueOf(page.getHtml().xpath("//div[@id=listnav]/ul/li[9]").regex("/inha/(\\d+)/").get());
            List<String> urls = new ArrayList<>();
            for (int i = 2; i <= last; i++) {
                urls.add("http://www.kuaidaili.com/free/inha/" + i + "/");
            }
            page.addTargetRequests(urls);
            flag = false;
        }
        List<String> locations = page.getHtml().xpath("//td[@data-title='位置']/text()").all();
        List<String> ports = page.getHtml().xpath("//td[@data-title='PORT']/text()").all();
        List<String> ips = page.getHtml().xpath("//td[@data-title='IP']/text()").all();
        for (int i = 0; i < locations.size(); i++) {
            if (ConnectTest.connectTest(ips.get(i), Integer.valueOf(ports.get(i)))) {
                proxy.put(ips.get(i), ports.get(i));
            }
        }
        redis.hsetWithExpired("proxy:hash", proxy);
        // "http://www\\.kuaidaili\\.com/free/inha/\\d+/"
        // http://gs.dlut.edu.cn/index/zytz.htm
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override 
    public void run() {
        Spider.create(this)
        .addUrl("http://www.kuaidaili.com/free/inha/1/")
        .thread(8)
        .run();
        System.out.println(proxy.size());
        for (String key : proxy.keySet()) {
            System.out.println(key + " : " + proxy.get(key));
        }
    }
}
