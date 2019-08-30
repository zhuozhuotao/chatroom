package com.bittrch.java.vo;

import lombok.Data;

/**
 * @Description:服务器与客户端传递信息载体
 */
@Data
public class MessageVo {
    /**
     * type:告知服务器要进行的动作。1：注册。2：私聊。
     * content:发送到服务器的具体内容
     * to：私聊告知服务器要将信息发给那个用户。
     */
    private String type;
    private String content;
    private String to;
}
