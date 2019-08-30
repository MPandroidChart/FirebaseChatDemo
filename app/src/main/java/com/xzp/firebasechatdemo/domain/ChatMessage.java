package com.xzp.firebasechatdemo.domain;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage {
    private String msg_user,msg_txt;
    private String msg_time;
    private boolean has_img;
    private String msg_path;
    private String channel_name;
    private String collection_name;
    private String docId;

    public ChatMessage() {
    }

    public String getMsg_path() {
        return msg_path;
    }

    public void setMsg_path(String msg_path) {
        this.msg_path = msg_path;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getCollection_name() {
        return collection_name;
    }

    public void setCollection_name(String collection_name) {
        this.collection_name = collection_name;
    }

    public ChatMessage(String msg_user, String msg_txt, String msg_time, boolean has_img, String msg_path, String channel_name, String collection_name, String docId) {
        this.msg_user = msg_user;
        this.msg_txt = msg_txt;
        this.msg_time = msg_time;
        this.has_img = has_img;
        this.msg_path = msg_path;
        this.channel_name = channel_name;
        this.collection_name = collection_name;
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public boolean isHas_img() {
        return has_img;
    }

    public void setHas_img(boolean has_img) {
        this.has_img = has_img;
    }

    public String getMsg_user() {
        return msg_user;
    }

    public void setMsg_user(String msg_user) {
        this.msg_user = msg_user;
    }

    public String getMsg_txt() {
        return msg_txt;
    }

    public void setMsg_txt(String msg_txt) {
        this.msg_txt = msg_txt;
    }

    public String getMsg_time() {
        return msg_time;
    }

    public void setMsg_time(String msg_time) {
        this.msg_time = msg_time;
    }
}
