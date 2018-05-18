package com.weibo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weibo.domain.Weibo;

/**
*@author: Menghui Chen
*@version: 2018年5月17日下午5:24:10
**/
public interface WeiboRepository extends JpaRepository<Weibo, Integer>{
    Weibo findByWid(String wid);
}
