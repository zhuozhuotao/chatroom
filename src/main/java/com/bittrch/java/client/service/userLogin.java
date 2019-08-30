package com.bittrch.java.client.service;

import com.bittrch.java.client.dao.AccountDao;
import com.bittrch.java.client.entity.User;
import com.bittrch.java.util.CommUtils;
import com.bittrch.java.vo.MessageVo;
import sun.plugin2.message.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Set;

public class userLogin {
    private JPanel userLogin;
    private JPanel userPanel;
    private JTextField userNameText;
    private JPasswordField passwordText;
    private JPanel BtnPanel;
    private JButton regBtn;
    private JButton loginBtn;
    private JFrame frame;
    private AccountDao accountDao = new AccountDao();

    public userLogin() {
        frame = new JFrame("用户登录");
        frame.setContentPane(userLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        //注册按钮
        regBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //弹出注册页面
                new UserReg();
            }
        });

        //登录按钮
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //1.校验用户信息
                String username = userNameText.getText();
                String password = String.valueOf(passwordText.getPassword());
                User user = accountDao.userLogin(username, password);
                System.out.println("开始检验登录了么");
                //2.成功，加载用户列表
                if (user != null) {
                    //创建json的格式：private static final Gson GSON = new GsonBuilder().create();
                    //2.1.提示成功
                    JOptionPane.showMessageDialog(frame, "登录成功", "提示信息",
                            JOptionPane.INFORMATION_MESSAGE);
                    //2.2当前页面不可见
                    frame.setVisible(false);
                    //2.3与服务器建立连接，将当前用户的用户名与密码发给客户端
                    Connect2Server connect2Server = new Connect2Server();
                    MessageVo messageVo = new MessageVo();
                    messageVo.setType("1");
                    messageVo.setContent(username);
                    String json2Server = CommUtils.object2Json(messageVo);
                    try {
                        PrintStream out = new PrintStream(connect2Server.getOut(),
                                true, "UTF-8");
                        out.println(json2Server);

                        //加载用户列表---
                        //将当前用户名、所有在线好友、与服务器建立的连接传递到用户列表界面
                        // 读取服务端发回的所有在线用户信息
                        Scanner in = new Scanner(connect2Server.getIn());
                        if (in.hasNextLine()) {
                            String msgFromServerStr = in.nextLine();
                            MessageVo msgFromServer =
                                    (MessageVo) CommUtils.json2Object(msgFromServerStr,
                                            MessageVo.class);
                            Set<String> users =
                                    (Set<String>) CommUtils.json2Object(msgFromServer.getContent(),
                                            Set.class);
                            System.out.println("所有在线用户为:" + users);
                            // 加载用户列表界面
                            // 将当前用户名、所有在线好友、与服务器建立连接传递到好友列表界面
                            new FriendsList(username, users, connect2Server);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                    //此时客户端已将信息发给了服务器，服务器接收在run()方法里实现。
                } else {
                    //3.失败，停留在当前页面，提示用户信息错误
                    JOptionPane.showMessageDialog(frame, "错误信息", "登录失败",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        userLogin u = new userLogin();
    }
}
