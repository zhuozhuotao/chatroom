package com.bittrch.java.client.service;

import com.bittrch.java.client.dao.AccountDao;
import com.bittrch.java.client.entity.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserReg {
    private JPanel userRegPanel;
    private JTextField userNameText;
    private JPasswordField passwordText;
    private JTextField briefText;
    private JButton regButton;
    private AccountDao accountDao = new AccountDao();

    public UserReg() {
        JFrame frame = new JFrame("用户注册");
        frame.setContentPane(userRegPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);

        //点击注册按钮获取注册信息将数据持久化到数据库，成功弹一个提示框
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //1.获取用户输入的注册信息
                String userName = userNameText.getText();
                String password = String.valueOf(passwordText.getPassword());
                String brief = briefText.getText();

                //2.将输入信息包装为User类，保存到数据库中
                User user = new User();
                user.setUsername(userName);
                user.setPassword(password);
                user.setBrief(brief);

                //3.调用dao对象
                if (accountDao.userReg(user)) {
                    JOptionPane.showMessageDialog(frame, "注册成功", "提示信息",
                            JOptionPane.INFORMATION_MESSAGE);
                    frame.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(frame, "注册失败", "失败信息",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
