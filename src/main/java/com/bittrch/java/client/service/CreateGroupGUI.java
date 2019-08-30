package com.bittrch.java.client.service;

import com.bittrch.java.util.CommUtils;
import com.bittrch.java.vo.MessageVo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateGroupGUI {
    private JPanel CreateGroupPanel;
    private JPanel friendsLabelPanel;
    private JTextField groupNameText;
    private JButton conformBtn;

    private String myName;
    private Set<String> friends;
    private Connect2Server connect2Server;
    private FriendsList friendsList;

    public CreateGroupGUI(String myName, Set<String> friends, Connect2Server connect2Server,
                          FriendsList friendsList) {
        this.myName = myName;
        this.friends = friends;
        this.connect2Server = connect2Server;
        this.friendsList = friendsList;
        JFrame frame = new JFrame("创建群组");
        frame.setContentPane(CreateGroupPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.pack();
        frame.setVisible(true);

        //将在线好友以checkBox的形式展示到界面中。
        friendsLabelPanel.setLayout(new BoxLayout(friendsLabelPanel, BoxLayout.Y_AXIS));
        JCheckBox[] checkBoxes = new JCheckBox[friends.size()];
        Iterator<String> iterator = friends.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String labelName = iterator.next();
            checkBoxes[i] = new JCheckBox(labelName);
            friendsLabelPanel.add(checkBoxes[i]);
            i++;
        }
        friendsLabelPanel.revalidate();

        //点击条件按钮将信息提交到服务器
        conformBtn.addActionListener(new ActionListener() {
            //1.
            @Override
            public void actionPerformed(ActionEvent e) {

//                4.1判断哪些好友选中加入群聊。
                Set<String> selectedFriends = new HashSet<>();
                Component[] comps = friendsLabelPanel.getComponents();
                for (Component comp : comps
                        ) {
                    JCheckBox checkBox = (JCheckBox) comp;
                    if (checkBox.isSelected()) {
                        String labelName = checkBox.getText();
                        //把labelName放入集合中，表示选中了
                        selectedFriends.add(labelName);
                    }
                }
                selectedFriends.add(myName);
//                4.2获取群名输入框输入的群名称
                String groupName = groupNameText.getText();
//                4.3将群名+选中好友信息发送到服务端
                MessageVo messageVo = new MessageVo();
                messageVo.setType("3");
                messageVo.setContent(groupName);
                messageVo.setTo(CommUtils.object2Json(selectedFriends));
                try {
                    PrintStream out = new PrintStream(connect2Server.getOut(),
                            true, "UTF-8");
                    out.println(CommUtils.object2Json(messageVo));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                //4.4将当前创建群的界面隐藏掉，刷新好友列表界面的群列表
                frame.setVisible(false);
                //addGroupInfo    添加群信息
                //loadGroupList   加载群聊列表
                friendsList.addGroup(groupName, selectedFriends);
                friendsList.loadGroupList();
            }
        });
    }


}
