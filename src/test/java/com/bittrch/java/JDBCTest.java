package com.bittrch.java;

import java.sql.*;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.bittrch.java.util.CommUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Properties;

public class JDBCTest {
    private static DruidDataSource dataSource;

    static {
        Properties props = CommUtils.
                loadProperties("datasource.properties");
        try {
            dataSource = (DruidDataSource) DruidDataSourceFactory.
                    createDataSource(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQuery() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = (Connection) dataSource.getPooledConnection();
            String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
            statement = connection.prepareStatement(sql);
            String user = "test' --";
            String pass = "6666666";
            statement.setString(1, user);
            statement.setString(2, pass);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println("登录成功");
            } else {
                System.out.println("登录失败");
            }
        } catch (SQLException e) {

        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    @Test
    public void testInsert() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = (Connection) dataSource.getPooledConnection();
            String password = DigestUtils.md5Hex("123");
            String sql = "INSERT INTO user(username, password,brief) " +
                    " VALUES (?,?,?)";
            statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "test1");
            statement.setString(2, password);
            statement.setString(3, "还是帅!");
            int rows = statement.executeUpdate();
            Assert.assertEquals(1, rows);
        } catch (SQLException e) {

        } finally {
            closeResources(connection, statement);
        }
    }

    @Test
    public void testLogin() {
        String userName = "test' -- ";
        String password = "6666666";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = (Connection) dataSource.getPooledConnection();
            String sql = "SELECT * FROM user WHERE username = '" + userName + "" +
                    " AND password = '" + password + "'";
            System.out.println(sql);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                System.out.println("登录成功!");
            } else {
                System.out.println("登录失败");
            }
        } catch (SQLException e) {

        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    //用于insert/update/delete的资源关闭
    public void closeResources(Connection connection,
                               Statement statement) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //用于select的关闭资源
    public void closeResources(Connection connection,
                               Statement statement,
                               ResultSet resultSet) {
        closeResources(connection, statement);
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}