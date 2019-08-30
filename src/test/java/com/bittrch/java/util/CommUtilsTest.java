package com.bittrch.java.util;

import com.bittrch.java.client.entity.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class CommUtilsTest {

    @Test
    public void loadProperties() {
        String fileName = "datasource.properties";
        Properties properties = CommUtils.loadProperties(fileName);
        Assert.assertNotNull(properties);
    }

    @Test
    public void object2Json() {
        User user = new User();
        user.setId(1);
        user.setUsername("test");
        user.setPassword("123");
        user.setBrief("hello");
        String str = CommUtils.object2Json(user);
        System.out.println(str);
    }

    @Test
    public void json2Object() {
        String jsonStr = "";
        User user = (User) CommUtils.json2Object(jsonStr, User.class);
        System.out.println(user);
    }
}