package com.weibo.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.weibo.dao.PeopleRepository;
import com.weibo.dao.RedisDao;
import com.weibo.domain.People;
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
*@version: 2018年5月18日上午10:31:17
**/
@Service    
public class PeopleSearch implements PageProcessor, Runnable{
    private static final Logger log = LoggerFactory.getLogger(PeopleSearch.class);
    private static final String PREFIX = "https://weibo.cn";
    private static final String PATTERN = "https://weibo.cn/account/privacy/tags/?uid={0}&st=cc817b";
    @Autowired
    RedisDao redis;
    @Autowired
    public PeopleRepository peopleRepository;
    private Site site = Site.me().setRetryTimes(3).setSleepTime(10000).setTimeOut(5000)
            .addHeader("Cookie", "");
    HttpClientDownloader httpClientDownloader = new HttpClientDownloader();

    @Override
    public void process(Page page) {
        if (page.getUrl().toString().endsWith("info")) {
            parseInfo(page);
        } else if (page.getUrl().toString().contains("account")) {
            parseTags(page);
        } else {
            parseHome(page);
        }
        
    }
    
    public void parseInfo(Page page) {
        log.info("info parse==================================================================");
        String pid = page.getUrl().toString().split("/")[3];
        People people = peopleRepository.findByPid(pid);
        people.setSex(page.getHtml().regex("性别:(\\S+)").get());
        people.setLocation(page.getHtml().regex("地区:(\\S+)").get());
        people.setBirth(page.getHtml().regex("生日:(\\S+)").get());
        peopleRepository.save(people);
    }
    
    public void parseHome(Page page) {
        log.info("home parse+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        People people = peopleRepository.findByHome(page.getUrl().get());
        String v = page.getHtml().xpath("//div[@class='u']").xpath("//img[@alt='V']").get();
        if (v != null) {
            people.setV(1);
            if (v.contains("5337")) {
                people.setOranization(1);
            } else {
                people.setOranization(0);
            }
        } else {
            people.setV(0);
        }
        people.setBlogs(Integer.valueOf(page.getHtml().xpath("//div[@class='tip2']").regex("微博\\[(\\d+)\\]").get()));
        people.setFollows(Integer.valueOf(page.getHtml().xpath("//div[@class='tip2']").regex("关注\\[(\\d+)\\]").get()));
        people.setFans(Integer.valueOf(page.getHtml().xpath("//div[@class='tip2']").regex("粉丝\\[(\\d+)\\]").get()));
        String suffix = page.getHtml().xpath("//div[@class='u']").regex("(/\\d+/info)").get();
        people.setPid(suffix.split("/")[1]);
        page.addTargetRequest(PREFIX + suffix);
        page.addTargetRequest(MessageFormat.format(PATTERN, suffix.split("/")[1]));
        peopleRepository.save(people);
    }
    
    public void parseTags(Page page) {
        log.info("tags parse~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        String pid = page.getUrl().regex("uid=(\\d+)&").get();
        People people = peopleRepository.findByPid(pid);
        List<String> tags = page.getHtml().xpath("//div[@class='c']").regex("stag=1\">(\\S+)</a>&nbsp;").all();
         
        if (tags == null || tags.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < tags.size(); i++) {
            sb.append(tags.get(i));
            if (i != tags.size() - 1) {
                sb.append(",");
            }
        }
        people.setTags(sb.toString());
        peopleRepository.save(people);
    }
    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void run() {
        
        List<People> peopleList = peopleRepository.findAll();
        String[] targets = new String[peopleList.size()];
        for (int i = 0; i < peopleList.size(); i++) {
            targets[i] = peopleList.get(i).getHome();
        }
        Map<String, String> proxys = ConnectTest.init(redis);
        if (proxys != null && proxys.size() != 0) {
            httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(ConnectTest.mapToProxy(proxys)));
        } 
//        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", 1080)));
        Spider.create(this)
        .setDownloader(httpClientDownloader)
        .addUrl(targets)
        .thread(4)
        .run();
    }
    

}
