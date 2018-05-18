package com.weibo.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.weibo.dao.PeopleRepository;
import com.weibo.dao.WeiboRepository;
import com.weibo.domain.People;
import com.weibo.domain.Weibo;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
*@author: Menghui Chen
*@version: 2018年5月17日下午4:51:36
**/
@Service
public class WeiboSearch implements PageProcessor, Runnable{
    private static final Logger log = LoggerFactory.getLogger(WeiboSearch.class);
    private AtomicInteger counter = new AtomicInteger(1);
    @Autowired
    public PeopleRepository peopleRepository;
    @Autowired
    public WeiboRepository weiboRepository;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);   
    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000).setTimeOut(10000)
            .addHeader("Cookie", "");
    private String target = "https://weibo.cn/search/mblog?hideSearchFrame=&keyword"
            + "=%E6%BB%B4%E6%BB%B4&advancedfilter=1&starttime=20180510&endtime=20180512&sort=time&page=";
    
    @Override
    public void process(Page page) {
        if (counter.intValue() >= 51) {
            return;
        }
        
        page.addTargetRequest(target + counter.getAndIncrement());
//        System.out.println(page.getHtml().css("div.c").xpath("//div[@id]").all());
        List<String> idList = page.getHtml().css("div.c").xpath("//div[@id]/@id").all();
        System.out.println(idList);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
        //主页
        List<String> homeList = page.getHtml().xpath("//a[@class='nk']/@href").all();
        //微博名
        List<String> nameList = page.getHtml().xpath("//a[@class='nk']/html()").all();
        List<String> tmp = page.getHtml().xpath("//span[@class='ct']/html()").all();
        //发布时间
        List<String> timeList = timeFormat(tmp);
        tmp = page.getHtml().css("div.c").xpath("//div[@id]").xpath("//span[@class='ctt']/text()").all();
        List<String> textList = new ArrayList<>();
        for (String text : tmp) {
            textList.add(text.substring(1));
        }

        //赞
        List<String> upvoteList = page.getHtml().css("div.c").xpath("//div[@id]").regex("赞\\[(\\d+)\\]</a>").all();
        //评论
        List<String> commentList = page.getHtml().css("div.c").xpath("//div[@id]").regex("[^原文]评论\\[(\\d+)\\]</a>").all();
        //转发
        List<String> forwardList = page.getHtml().css("div.c").xpath("//div[@id]").regex("转发\\[(\\d+)\\]</a>").all();
        for (int i = 0; i < homeList.size(); i++) {
            String wid = idList.get(i);
            
            Weibo wb = weiboRepository.findByWid(wid);
            if (wb == null) {
                People people = peopleRepository.findByHome(homeList.get(i));
                if (people == null) {
                    people = new People();
                    people.setHome(homeList.get(i));
                    people.setName(nameList.get(i));
                    peopleRepository.save(people);
                } 
                wb = new Weibo();
                wb.setWid(wid);
                wb.setAuthor(people);
                wb.setTime(timeList.get(i));
                wb.setText(textList.get(i));
                wb.setUpvote(Integer.valueOf(upvoteList.get(i)));
                wb.setComment(Integer.valueOf(commentList.get(i)));
                wb.setForward(Integer.valueOf(forwardList.get(i)));
                weiboRepository.save(wb);
                log.info("处理中=================================");
            } else {
                log.info("该条微博已存在！！！！！");
            }
            
        }
        
    }
    @Override
    public Site getSite() {
        return site;
    }
    public List<String> timeFormat(List<String> list) {
        List<String> ret = new ArrayList<>();
        for (String time : list) {
            ret.add(timeFormat(time));
        }
        return ret;
    }
    public String timeFormat(String str) {
        if (str.contains("&nbsp;")) {
            str = str.split("&nbsp;")[0].trim();
        } else {
            str = str.trim();
        }
        Calendar  now = Calendar.getInstance();
        
        if (str.contains("分钟前")) {
            String s = str.split("分钟前")[0];
            now.add(Calendar.MINUTE, Integer.valueOf(s) * -1);
            return df.format(now.getTime());
        } else if (str.contains("今天")){
            str = str.split("今天")[1];
            String[] time = str.split(":");
            now.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0].trim()));
            now.set(Calendar.MINUTE, Integer.valueOf(time[1].trim()));
            return df.format(now.getTime());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(now.get(Calendar.YEAR)).append("年").append(str);
            return sb.toString();
        }
        
    }
    @Override
    public  void run() {
        Spider.create(this)
        .addUrl(target + counter.getAndIncrement())
        .thread(8)
        .run();
    }
}
