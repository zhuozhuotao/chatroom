package com.bittrch.java.client.service;

import com.bittrch.java.util.CommUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class Connect2Server {
    private static final String IP;
    private static final int PORT;
    //存当前服务器所有在线用户的信息
    private static Map<String, Socket> clients = new ConcurrentHashMap<>();

    static {   //把IP和端口号通过静态代码块的方式加载到程序中
        Properties properties = CommUtils.loadProperties("socket.properties");
        IP = properties.getProperty("adderss");
        PORT = Integer.parseInt(properties.getProperty("port"));
    }

    private Socket client;
    private InputStream in;
    private OutputStream out;

    public Connect2Server() {
        try {
            client = new Socket(IP, PORT);
            in = client.getInputStream();
            out = client.getOutputStream();
        } catch (IOException e) {
            System.err.println("与服务器建立连接失败");
            e.printStackTrace();
        }
    }

    //获取建立连接后输入输出流传递数据
    public InputStream getIn() {   //获取服务器发来的数据
        return in;
    }

    public OutputStream getOut() {   //给服务器发数据
        return out;
    }
}
