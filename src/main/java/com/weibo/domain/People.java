package com.weibo.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
*@author: Menghui Chen
*@version: 2018年5月17日下午5:05:36
**/
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
public class People {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    private String home;
    @Column(length = 100)  
    private String pid;
    private String sex;
    private int fans;
    private int follows;
    private int v;
    private String name;
    private int blogs;
    private String location;
    private String birth;
    private String tags;
    private int oranization;
}

