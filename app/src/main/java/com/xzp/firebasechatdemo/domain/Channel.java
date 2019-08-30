package com.xzp.firebasechatdemo.domain;

public class Channel {
    private String channel_name;
    private String channel_time;
    private String channel_msg;
    private int chat_icon;

    public Channel(String channel_name, String channel_time, String channel_msg, int chat_icon) {
        this.channel_name = channel_name;
        this.channel_time = channel_time;
        this.channel_msg = channel_msg;
        this.chat_icon = chat_icon;
    }

    public int getChat_icon() {
        return chat_icon;
    }

    public void setChat_icon(int chat_icon) {
        this.chat_icon = chat_icon;
    }

    public Channel() {
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getChannel_time() {
        return channel_time;
    }

    public void setChannel_time(String channel_time) {
        this.channel_time = channel_time;
    }

    public String getChannel_msg() {
        return channel_msg;
    }

    public void setChannel_msg(String channel_msg) {
        this.channel_msg = channel_msg;
    }
}
