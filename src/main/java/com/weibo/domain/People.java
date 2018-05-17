package com.weibo.domain;

import javax.persistence.Entity;
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
    private String home;
    private int id;
    private int sex;
    private int age;
    private int fans;
    private int follows;
    private int v;
    private String name;
}

