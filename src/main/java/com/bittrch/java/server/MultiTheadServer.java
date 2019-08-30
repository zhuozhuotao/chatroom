package com.bittrch.java.server;

import com.bittrch.java.util.CommUtils;
import com.bittrch.java.vo.MessageVo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:聊天室服务端
 */
public class MultiTheadServer {
    private static final String IP;
    private static final int PORT;
    //存当前服务器所有在线用户的信息
    private static Map<String, Socket> clients = new ConcurrentHashMap<>();
    // 缓存当前服务器注册的所有群名称以及群好友
    private static Map<String, Set<String>> groups = new ConcurrentHashMap<>();

    static {   //把IP和端口号通过静态代码块的方式加载到程序中
        Properties properties = CommUtils.loadProperties("socket.properties");
        IP = properties.getProperty("adderss");
        PORT = Integer.parseInt(properties.getProperty("port"));
    }

    //处理客户端连接时需要新建新的线程去处理，不然accept()会一直阻塞
    //因此新建一个内部类
    private static class ExecuteClient implements Runnable {
        private Socket client;
        private Scanner in;
        private PrintStream out;

        public ExecuteClient(Socket client) {
            this.client = client;
            try {
                this.in = new Scanner(client.getInputStream());
                this.out = new PrintStream(client.getOutputStream()
                        , true, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                if (in.hasNextLine()) {
                    String jsonStrFromClient = in.nextLine();
                    MessageVo msgFromClient =
                            (MessageVo) CommUtils.json2Object(jsonStrFromClient,
                                    MessageVo.class);
                    // 新用户注册到服务端
                    if (msgFromClient.getType().equals("1")) {
                        String userName = msgFromClient.getContent();
                        // 将当前在线的所有用户名发回客户端
                        MessageVo msg2Client = new MessageVo();
                        msg2Client.setType("1");
                        msg2Client.setContent(CommUtils.object2Json(
                                clients.keySet()
                        ));
                        out.println(CommUtils.object2Json(msg2Client));
                        // 将新上线的用户信息发回给当前已在线的所有用户
                        sendUserLogin("newLogin:" + userName);
                        // 将当前新用户注册到服务端缓存
                        clients.put(userName, client);
                        System.out.println(userName + "上线了!");
                        System.out.println("当前聊天室共有" +
                                clients.size() + "人");
                    } else if (msgFromClient.getType().equals("2")) {
                        // 用户私聊
                        // type:2
                        //  Content:myName-msg
                        //  to:friendName
                        String friendName = msgFromClient.getTo();
                        Socket clientSocket = clients.get(friendName);
                        try {
                            PrintStream out = new PrintStream(clientSocket.getOutputStream(),
                                    true, "UTF-8");
                            MessageVo msg2Client = new MessageVo();
                            msg2Client.setType("2");
                            msg2Client.setContent(msgFromClient.getContent());
                            System.out.println("收到私聊信息,内容为" + msgFromClient.getContent());
                            out.println(CommUtils.object2Json(msg2Client));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (msgFromClient.getType().equals("3")) {
                        // 注册群
                        String groupName = msgFromClient.getContent();
                        // 该群的所有群成员
                        //字符串变为Set集合。
                        Set<String> friends = (Set<String>) CommUtils.json2Object(
                                msgFromClient.getTo(), Set.class);
                        groups.put(groupName, friends);
                        System.out.println("有新的群注册成功,群名称为" +
                                groupName + ",一共有" + groups.size() + "个群");
                    } else if (msgFromClient.getType().equals("4")) {
                        // 群聊信息
                        System.out.println("服务器收到的群聊信息为:" + msgFromClient);
                        String groupName = msgFromClient.getTo();
                        Set<String> names = groups.get(groupName);
                        Iterator<String> iterator = names.iterator();
                        while (iterator.hasNext()) {
                            String socketName = iterator.next();
                            Socket client = clients.get(socketName);
                            try {
                                PrintStream out = new PrintStream(client.getOutputStream(),
                                        true, "UTF-8");
                                MessageVo messageVO = new MessageVo();
                                messageVO.setType("4");
                                messageVO.setContent(msgFromClient.getContent());
                                // 群名-[]
                                messageVO.setTo(groupName + "-" + CommUtils.object2Json(names));
                                out.println(CommUtils.object2Json(messageVO));
                                System.out.println("服务端发送的群聊信息为:" + messageVO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }

        /**
         * 向所有在线用户发送新用户上线信息
         *
         * @param msg
         */
        private void sendUserLogin(String msg) {
            for (Map.Entry<String, Socket> entry : clients.entrySet()) {
                Socket socket = entry.getValue();
                try {
                    PrintStream out = new PrintStream(socket.getOutputStream(),
                            true, "UTF-8");
                    out.println(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService executors = Executors.
                newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            System.out.println("等待客户端连接...");
            Socket client = serverSocket.accept();
            System.out.println("有新的连接,端口号为" + client.getPort());
            //把具体连接交给线程处理，然后循环等待新的客户端。
            executors.submit(new ExecuteClient(client));
        }
    }
}
