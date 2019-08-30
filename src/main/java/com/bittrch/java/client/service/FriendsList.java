package com.bittrch.java.client.service;

import com.bittrch.java.util.CommUtils;
import com.bittrch.java.vo.MessageVo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsList {
    private JPanel friendsPanel;
    private JScrollPane friendsList;
    private JFrame frame;
    private JButton createGroupBtn;
    private JScrollPane groupListPanel;

    private String userName;
    // 存储所有在线好友
    private Set<String> users;
    // 存储所有群名称以及群好友
    private Map<String,Set<String>> groupList = new ConcurrentHashMap<>();
    private Connect2Server connect2Server;
    // 缓存所有私聊界面
    private Map<String,PrivateChatGUI> privateChatGUIList = new ConcurrentHashMap<>();
    // 缓存所有群聊界面
    private Map<String,GroupChatGUI> groupChatGUIList = new ConcurrentHashMap<>();
    // 好友列表后台任务，不断监听服务器发来的信息
    // 监听好友上线信息、用户私聊、群聊
    private class DaemonTask implements Runnable {
        private Scanner in = new Scanner(connect2Server.getIn());
        @Override
        public void run() {
            while (true) {
                // 收到服务器发来的信息
                if (in.hasNextLine()) {
                    String strFromServer = in.nextLine();
                    // 此时服务器发来的是一个json字符串
                    if (strFromServer.startsWith("{")) {
                        MessageVo messageVO = (MessageVo) CommUtils.json2Object(strFromServer,
                                MessageVo.class);
                        if (messageVO.getType().equals("2")) {
                            // 服务器发来的私聊信息
                            String friendName = messageVO.getContent().split("-")[0];
                            String msg = messageVO.getContent().split("-")[1];
                            // 判断此私聊是否是第一次创建
                            if (privateChatGUIList.containsKey(friendName)) {
                                PrivateChatGUI privateChatGUI = privateChatGUIList.get(friendName);
                                privateChatGUI.getFrame().setVisible(true);
                                privateChatGUI.readFromServer(friendName+"说:"+msg);
                            }else {
                                PrivateChatGUI privateChatGUI = new PrivateChatGUI(friendName,
                                        userName,connect2Server);
                                privateChatGUIList.put(friendName,privateChatGUI);
                                privateChatGUI.readFromServer(friendName+"说:"+msg);
                            }
                        }
                        else if (messageVO.getType().equals("4")) {
                            // 收到服务器发来的群聊信息
                            // type:4
                            // content:sender-msg
                            // to:groupName-[1,2,3,...]
                            String groupName = messageVO.getTo().split("-")[0];
                            String senderName = messageVO.getContent().split("-")[0];
                            String groupMsg = messageVO.getContent().split("-")[1];
                            // 若此群名称在群聊列表
                            if (groupList.containsKey(groupName)) {
                                if (groupChatGUIList.containsKey(groupName)) {
                                    // 群聊界面弹出
                                    GroupChatGUI groupChatGUI = groupChatGUIList.get(groupName);
                                    groupChatGUI.getFrame().setVisible(true);
                                    groupChatGUI.readFromServer(senderName+"说:"+groupMsg);
                                }else {
                                    Set<String> names = groupList.get(groupName);
                                    GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,
                                            names,userName,connect2Server);
                                    groupChatGUIList.put(groupName,groupChatGUI);
                                    groupChatGUI.readFromServer(senderName+"说:"+groupMsg);
                                }
                            }else {
                                // 若群成员第一次收到群聊信息
                                // 1.将群名称以及群成员保存到当前客户端群聊列表
                                Set<String> friends = (Set<String>) CommUtils.json2Object(messageVO.getTo().split("-")[1],
                                        Set.class);
                                groupList.put(groupName, friends);
                                loadGroupList();
                                // 2.弹出群聊界面
                                GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,
                                        friends,userName,connect2Server);
                                groupChatGUIList.put(groupName,groupChatGUI);
                                groupChatGUI.readFromServer(senderName+"说:"+groupMsg);
                            }


                        }
                    }else {
                        // newLogin:userName
                        if (strFromServer.startsWith("newLogin:")) {
                            String newFriendName = strFromServer.split(":")[1];
                            users.add(newFriendName);
                            // 弹框提示用户上线
                            JOptionPane.showMessageDialog(frame,
                                    newFriendName+"上线了!",
                                    "上线提醒",JOptionPane.INFORMATION_MESSAGE);
                            // 刷新好友列表
                            loadUsers();
                        }
                    }
                }
            }
        }
    }
    // 私聊点击事件
    private class PrivateLabelAction implements MouseListener {
        private String labelName;

        public PrivateLabelAction(String labelName) {
            this.labelName = labelName;
        }

        // 鼠标点击执行事件
        @Override
        public void mouseClicked(MouseEvent e) {
            // 判断好友列表私聊界面缓存是否已经有指定标签
            //（界面被设置为隐藏）
            if (privateChatGUIList.containsKey(labelName)) {
                PrivateChatGUI privateChatGUI = privateChatGUIList.get(labelName);
                privateChatGUI.getFrame().setVisible(true);
            }else {
                // 第一次点击，创建私聊界面
                PrivateChatGUI privateChatGUI = new PrivateChatGUI(
                        labelName,userName,connect2Server
                );
                privateChatGUIList.put(labelName,privateChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    // 群聊点击事件
    private class GroupLabelAction implements MouseListener {
        private String groupName;

        public GroupLabelAction(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (groupChatGUIList.containsKey(groupName)) {
                GroupChatGUI groupChatGUI = groupChatGUIList.get(groupName);
                groupChatGUI.getFrame().setVisible(true);
            }else {
                //获取群好友
                Set<String> names = groupList.get(groupName);
                GroupChatGUI groupChatGUI = new GroupChatGUI(
                        groupName,names,userName,connect2Server
                );
                groupChatGUIList.put(groupName,groupChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public FriendsList(String userName, Set<String> users,
                       Connect2Server connect2Server) {
        this.userName = userName;
        this.users = users;
        this.connect2Server = connect2Server;
        frame = new JFrame(userName);
        frame.setContentPane(friendsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loadUsers();
        // 启动后台线程不断监听服务器发来的消息
        Thread daemonThread = new Thread(new DaemonTask());
        daemonThread.setDaemon(true);
        daemonThread.start();
        // 创建群组
        createGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateGroupGUI(userName,users,connect2Server,
                        FriendsList.this);
            }
        });
    }
    // 加载所有在线的用户信息
    public void loadUsers() {     //遍历set加label到panel
        JLabel[] userLabels = new JLabel [users.size()];
        JPanel friends = new JPanel();
        friends.setLayout(new BoxLayout(friends,BoxLayout.Y_AXIS));
        // set遍历
        Iterator<String> iterator = users.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String userName = iterator.next();
            userLabels[i] = new JLabel(userName);
            // 添加标签点击事件
            userLabels[i].addMouseListener(new PrivateLabelAction(userName));
            friends.add(userLabels[i]);
            i++;
        }
        friendsList.setViewportView(friends);
        // 设置滚动条垂直滚动
        friendsList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        friends.revalidate();
        friendsList.revalidate();
    }

    public void loadGroupList() {
        // 存储所有群名称标签Jpanel
        JPanel groupNamePanel = new JPanel();
        groupNamePanel.setLayout(new BoxLayout(groupNamePanel,
                BoxLayout.Y_AXIS));
        JLabel[] labels = new JLabel[groupList.size()];
        // Map遍历
        Set<Map.Entry<String,Set<String>>> entries = groupList.entrySet();
        Iterator<Map.Entry<String,Set<String>>> iterator =
                entries.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<String,Set<String>> entry = iterator.next();
            labels[i] = new JLabel(entry.getKey());
            labels[i].addMouseListener(new GroupLabelAction(entry.getKey()));
            groupNamePanel.add(labels[i]);
            i++;
        }
        groupListPanel.setViewportView(groupNamePanel);
        groupListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        groupListPanel.revalidate();
    }

    public void addGroup(String groupName,Set<String> friends) {
        groupList.put(groupName,friends);
    }
}
