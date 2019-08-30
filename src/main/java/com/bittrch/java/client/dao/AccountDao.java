package com.bittrch.java.client.dao;

import com.bittrch.java.client.entity.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;

/**
 * @Author:
 * @Date:
 * @Description:用户操作，写注册登录
 */

public class AccountDao extends BasedDao {
    //注册方法  insert
    public boolean userReg(User user) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            String sql = "insert into user(username,password,brief) values (?,?,?)";
            statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, DigestUtils.md5Hex(user.getPassword()));
            statement.setString(3, user.getBrief());
            int rows = statement.executeUpdate();

            if (rows == 1) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("用户注册失败");
            e.printStackTrace();
        } finally {
            closeResources(connection, statement);
        }
        return false;
    }

    //登录方法select
    public User userLogin(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();

            String sql = "select * from user where username=? and password=?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, DigestUtils.md5Hex(password));
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = getUser(resultSet);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("用户登录失败了");
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        return null;
    }

    private User getUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setBrief(resultSet.getString("brief"));
        return user;
    }
}
