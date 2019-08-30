package com.xzp.firebasechatdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xzp.firebasechatdemo.constants.MessageConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetNetTimeUtil {
    public  static final String TAG="GetNetTimeUtil";
    public static DateFormat dft = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");



    public  void  getNetTime(final  Handler handler){

        final String webUrl = "http://www.baidu.com";

        try {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    URL url = null;
                    try {
                        url = new URL(webUrl);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
                    URLConnection uc = null;
                    try {
                        uc = url.openConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
//                    uc.setReadTimeout(5000);
//                    uc.setConnectTimeout(5000);
                    try {
                        uc.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
                    long current_time = uc.getDate();
                    DateFormat dft = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
                    String time=dft.format(new Date(current_time));
                    Message msg=Message.obtain();
                    Bundle bundle=new Bundle();
                    bundle.putString("time",time);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    //handler.sendEmptyMessage(111);
                }
            }.start();



        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }

    }
}
