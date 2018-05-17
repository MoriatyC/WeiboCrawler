package com.weibo.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;import com.weibo.dao.PeopleRepository;
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
    @Autowired
    public PeopleRepository peopleRepository;
    @Autowired
    public WeiboRepository weiboRepository;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);   
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000)
            .addHeader("Cookie", "_T_WM=40e67082a2264b7627921f877496e8b7; "
                    + "SUB=_2A253-EAiDeRhGeVN4lAU8CnNzTiIHXVVA2BqrDV6PUJbkdBeLVn5kW1NTJSZ2CqRILEXoKbO-NYj-YlpAtHyl_bK;"
                    + " SUHB=0ebdbT23lJJzZH; "
                    + "SCF=AvnrxNGTzU0T2OyzByO_Gq5nvwAeCXn6ZLvvuu6GgRcgcXbxrmkKg_oPO0gcy4vMltckvQT7go2IwmLR2TDgXYo.");
    
    
    WeiboSearch weibo;
    @PostConstruct
    public void init() {
        weibo = this;
        weibo.peopleRepository = this.peopleRepository;
    }
    @Override
    public void process(Page page) {
//        System.out.println(page.getHtml().css("div.c").xpath("//div[@id]").all());
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
//        System.out.println(homeList.size());
//        System.out.println(nameList.size());
//        System.out.println(timeList);
//        System.out.println(timeList.size());
//        System.out.println(textList.size());
//        System.out.println(upvoteList.size());
//        System.out.println(commentList.size());
//        System.out.println(forwardList.size());
        for (int i = 0; i < homeList.size(); i++) {
            Weibo wb = new Weibo();
            People people = peopleRepository.findByHome(homeList.get(i));
            if (people == null) {
                people = new People();
                people.setHome(homeList.get(i));
                people.setName(nameList.get(i));
                peopleRepository.save(people);
            } 
            wb.setAuthor(people);
            wb.setTime(timeList.get(i));
            wb.setText(textList.get(i));
            wb.setUpvote(Integer.valueOf(upvoteList.get(i)));
            wb.setUpvote(Integer.valueOf(commentList.get(i)));
            wb.setUpvote(Integer.valueOf(forwardList.get(i)));
            weiboRepository.save(wb);
            System.out.println("=================================");
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
        .addUrl("https://weibo.cn/search/mblog?hideSearchFrame=&keyword=%E6%BB%B4%E6%BB%B4&advancedfilter=1&starttime=20180510&endtime=20180512&sort=time&page=1")
        .thread(5)
        .run();
    }
}
