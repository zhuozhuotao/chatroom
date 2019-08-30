package com.bittrch.java.client.dao;

import com.bittrch.java.client.entity.User;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;

import static org.junit.Assert.*;

/**
 * description：测试注册登录方法
 */
public class AccountDaoTest {

    private AccountDao accountDao = new AccountDao();

    @Test
    public void userReg() {
        User user = new User();
        user.setUsername("张一");
        user.setPassword("123");
        user.setBrief("hello");
        boolean flag = accountDao.userReg(user);
        Assert.assertTrue(flag);
    }

    @Test
    public void userLogin() {
        String username = "张一";
        String password = "123";
        User user = accountDao.userLogin(username, password);
        System.out.println(user);
        Assert.assertNull(user);
    }
}