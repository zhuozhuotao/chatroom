package com.bittrch.java.client.entity;

import lombok.Data;

import java.util.Set;

/**
 * @Author:
 * @Date:
 * @Description: 实体类
 */
@Data
public class User {
    private Integer id;   //使用包装类对应于数据库，Integer默认为mull
    private String username;
    private String password;
    private String brief;
}
