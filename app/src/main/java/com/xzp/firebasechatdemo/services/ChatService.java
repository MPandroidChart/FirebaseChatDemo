package com.xzp.firebasechatdemo.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.xzp.firebasechatdemo.MainActivity;
import com.xzp.firebasechatdemo.R;
import com.xzp.firebasechatdemo.adapters.ChatAdapter;
import com.xzp.firebasechatdemo.adapters.FirestoreAdapter;
import com.xzp.firebasechatdemo.constants.MessageConstants;
import com.xzp.firebasechatdemo.domain.ChatMessage;
import com.xzp.firebasechatdemo.utils.GetNetTimeUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private ChatAdapter madapter;
    private static final String TAG="ChatService.class";
    public static final String RESULT_KEY = "ok";
    private Bitmap bm;
    private MainActivity activity = new MainActivity();
    private String user;
    private String messageUser;
    private NotificationManager nm;
    private Query mQuery;
    private FirebaseFirestore mFirestore;
    private String path, txt, channelname, collectionname;
    private boolean sendMsg = false;
    public static final String ALLMESSAGEDATA = "AllMessageData";
    private boolean sendMsgbyNotification=false;
    private String docId;
    private String collection_path;
    private GetNetTimeUtil timeUtil=new GetNetTimeUtil();
    private String time,userName,reply;
    //    public class ChatBind extends Binder{

    //此方法在主线程中调用，可以更新UI
    Handler handler1 = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void handleMessage(Message msg) {
            // 处理消息时需要知道是成功的消息还是失败的消息
            switch (msg.what) {
                case 110:

                    if(!user.equals(messageUser)&&!messageUser.equals("Robot"))
                    buildNotification(txt, user, channelname, collectionname);
                    break;

            }

        }
    };
    Handler handler=new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle date = new Bundle();
            date = msg.getData();
            DateFormat dft = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            time = date.getString("time", dft.format(new Date()));
            try {
                activity.SaveAllChatMessageToDB(null, userName, time, false, reply,channelname,collectionname,docId);
                if (!TextUtils.isEmpty(collectionname))
                    activity.SaveChatMessageToDB(userName,time,false,reply,collectionname,channelname,docId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public Bitmap getBitMBitmap(final String urlpath) {

        new Thread() {
            public void run() {
                try {
                    URL url = new URL(urlpath);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    InputStream in;
                    in = conn.getInputStream();
                    bm = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
                handler1.sendEmptyMessage(110);
            }


        }.start();

        return bm;
    }


    public void getNewMessage() {

        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        madapter = new ChatAdapter(mQuery, this ){


            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onDataChanged(DocumentChange change) {
                super.onDataChanged(change);
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {


                } else {
                    ChatMessage model = change.getDocument().toObject(ChatMessage.class);
                    if(!TextUtils.isEmpty(path)){
                        path=null;
                    }
                    Log.e(TAG, model.getMsg_time());

                    if(model.isHas_img()) {
                        path = model.getMsg_path();
                    }
                    txt = model.getMsg_txt();
                    collectionname = model.getCollection_name();
                    channelname = model.getChannel_name();
                    messageUser=model.getMsg_user();
                    docId=model.getDocId();

                    if (!TextUtils.isEmpty(path)) {
                        bm = getBitMBitmap(path);
                    } else if (!TextUtils.isEmpty(txt)) {
                        if(bm!=null){
                            bm=null;

                        }
                        if(!user.equals(messageUser)&&!messageUser.equals("Robot"))
                        buildNotification(txt, user, channelname, collectionname);
                    }

                }


            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Log.e(TAG, "b");

            }

        };

        if (madapter != null) {
            madapter.startListening();
        }

    }

    private void initFirestore(String collection_name) {
        // TODO(developer): Implement
        mFirestore = FirebaseFirestore.getInstance();
        //获得评价最高的50家餐厅
        mQuery = mFirestore.collection(collection_name)
                .orderBy("msg_time", Query.Direction.ASCENDING);
        Log.e(TAG,"hhh"+mQuery.toString());

    }

    public ChatService() {


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 获取RemoteInput中的Result
        Bundle replyBundle = RemoteInput.getResultsFromIntent(intent);
        if (replyBundle != null) {
            // 根据key拿到回复的内容
//            if(!messageUser.equals(user)){
               

             reply = replyBundle.getString(RESULT_KEY);
            Log.e(TAG, reply + "reply");

            SharedPreferences sharedPreferences = getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
             userName = sharedPreferences.getString(MessageConstants.USER_NAME, "chat_01");

            timeUtil.getNetTime(handler);
            UpdataNotification();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nm.cancel(NOTIFICATION_ID);



        }



        return super.onStartCommand(intent, flags, startId);
    }

    private void UpdataNotification() {
        Notification notification = null;
        /**
         * android API 26 以上 NotificationChannel 特性适配
         */
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = createNotificationChannel();
            notification = new Notification.Builder(this, channel.getId())
                    .setContentText("发送成功！")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();

            notificationManager.createNotificationChannel(channel);

        } else {
            notification = new Notification.Builder(this)
                    .setContentText("发送成功！")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
        }
        notificationManager.notify(NOTIFICATION_ID,notification);


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    /**
     * 创建通知
     *
     * @return
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void buildNotification(String txt, String username, String channel_name_txt, String collection_name) {
        // step 1 创建RemoteInput
        RemoteInput remoteInput = new RemoteInput.Builder(RESULT_KEY)
                .setLabel("回复这条消息")
                .build();


        // step 2 创建pendingintent, 当发送时调用什么
        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra(MessageConstants.CHAT_ROOM_NAME, collection_name);
        intent.putExtra(MessageConstants.TXT_CHANNEL_NAME, channel_name_txt);
        PendingIntent pi = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // step 3 创建快捷回复 Action
        NotificationCompat.Action act = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "回复", pi)
                .addRemoteInput(remoteInput).build();

        NotificationCompat.BigPictureStyle picStyle = new NotificationCompat.BigPictureStyle();
        picStyle.bigPicture(bm);
        // picStyle.bigLargeIcon(bm);
        // step 4 创建notification
        // 使用设置优先级的方式创建悬浮通知，则会自动消失
        Notification notification;
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = ChatService.createNotificationChannel();
            builder = new NotificationCompat.Builder(this, channel.getId());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(channel_name_txt);
            if (!TextUtils.isEmpty(txt)) {
                builder.setContentText(messageUser + ":" + txt);
            } else {
                builder.setContentText(messageUser + ":");

            }
            builder.setColor(Color.CYAN);
            if (null != bm) {
                builder.setStyle(picStyle);
            }
            builder.setContentIntent(pi);
            builder.setPriority(Notification.PRIORITY_MAX);// 设置优先级为Max，则为悬浮通知
            builder.addAction(act); // 设置回复action
            builder.setAutoCancel(true);
            builder.setWhen(System.currentTimeMillis());
            builder.setDefaults(Notification.DEFAULT_ALL);// 想要悬浮出来， 这里必须要设置
            builder.setCategory(Notification.CATEGORY_MESSAGE);
            notification = builder.build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        } else {
            builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(channel_name_txt);
            if (!TextUtils.isEmpty(txt)) {
                builder.setContentText(messageUser + ":" + txt);
            } else {
                builder.setContentText(messageUser + ":");

            }
            builder.setColor(Color.CYAN);
            if (null != bm) {
                builder.setStyle(picStyle);
            }
            builder.setContentIntent(pi);
            builder.setPriority(Notification.PRIORITY_MAX);// 设置优先级为Max，则为悬浮通知
            builder.addAction(act);// 设置回复action
            builder.setAutoCancel(true);
            builder.setWhen(System.currentTimeMillis());
            builder.setDefaults(Notification.DEFAULT_ALL);// 想要悬浮出来， 这里必须要设置
            builder.setCategory(Notification.CATEGORY_MESSAGE);
            notification = builder.build();
        }
        nm.notify(NOTIFICATION_ID, notification);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel createNotificationChannel() {
        String channelId = "apple";
        String channelName = "AppleMusicService";
        String Description = "AppleMusic";
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(Description);
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        channel.setShowBadge(false);

        return channel;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (madapter != null) {
            madapter.stopListening();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        nm = getSystemService(NotificationManager.class);
        SharedPreferences sp = this.getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
        user = sp.getString(MessageConstants.USER_NAME, "null");
        Log.e(TAG,user+"name");
        initFirestore(ALLMESSAGEDATA);
        getNewMessage();

    }
}
