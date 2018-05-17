package com.weibo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weibo.domain.People;

/**
*@author: Menghui Chen
*@version: 2018年5月17日下午5:24:10
**/
public interface PeopleRepository extends JpaRepository<People, Integer>{
    People findByHome(String url);
}
