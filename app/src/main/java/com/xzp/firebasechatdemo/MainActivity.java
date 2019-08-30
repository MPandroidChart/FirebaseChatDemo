package com.xzp.firebasechatdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.xzp.firebasechatdemo.adapters.ChatAdapter;
import com.xzp.firebasechatdemo.constants.MessageConstants;
import com.xzp.firebasechatdemo.domain.ChatMessage;
import com.xzp.firebasechatdemo.domain.WeatherInfo;
import com.xzp.firebasechatdemo.services.ChatService;
import com.xzp.firebasechatdemo.services.MyUploadService;
import com.xzp.firebasechatdemo.utils.BitmapUtil;
import com.xzp.firebasechatdemo.utils.BitmapUtils;
import com.xzp.firebasechatdemo.utils.CompressUtils;
import com.xzp.firebasechatdemo.utils.GetNetTimeUtil;
import com.xzp.firebasechatdemo.utils.GetPathFromUri;
import com.xzp.firebasechatdemo.utils.JsonParase;
import com.xzp.firebasechatdemo.utils.Weather;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_IMAGE = 1;
    public static final String RESULT_KEY = "ok";
    public static final int NOTIFICATION_ID = 110;
    private EditText et_msg;
    private RecyclerView chat_msg_rv;
    private List<ChatMessage> datalist = new ArrayList<ChatMessage>();
    private Button bt_send_msg;
    private static final String TAG = "MainActivity.class";
    private List<String> list_userName = new ArrayList<String>();
    private List<String> list_chatMsg = new ArrayList<String>();
    private List<String> list_chatTime = new ArrayList<String>();
    private DocumentReference dr = FirebaseFirestore.getInstance().document("ChatData/Messages");
    private String username;
    private static final String MSG_USER = "msg_user";
    private static final String MSG_TIME = "msg_time";
    private static final String MSG_TXT = "msg_txt";
    private static final String MSG_PATH = "msg_path";
    private static final String HAS_ICON = "has_img";
    private static final String CHANNEL_NAME = "channel_name";
    private static final String COLLECTION_NAME = "collection_name";
    private static final String DOCID = "docId";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private ChatAdapter mAdapter;
    private static final int LIMIT = 50;
    private String collection_name;
    private ImageView iv_select_pic;
    private Uri mFileUri = null;
    private Uri mDownloadUrl = null;
    private BroadcastReceiver mBroadcastReceiver;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private TextView txt_channnel_name;
    private String channel_name_txt;
    private ChatService service;
    private ChatMessage chatMessage;
    private ChatAdapter adapter;
    private String notification_txt = null, notification_img_path = null;
    private List<ChatMessage> list_notification = new ArrayList<>();
    private boolean isFirst = false;
    private Bitmap bm;
    public static final String ALLMESSAGEDATA="AllMessageData";
    private List<FirebaseTextMessage>conversation=new ArrayList<>();
    private TextView smart_reply_txt1,smart_reply_txt2,smart_reply_txt3;
    private LinearLayout ly_smart_reply;
    private List<String>list_reply=new ArrayList<>();
    private boolean rebot_first_reply=true;
    private int i=0;
    private String docId;
    private String time;
    private String desc;
    private    String chat_msg;
    private GetNetTimeUtil timeUtil=new GetNetTimeUtil();
    //dialogflow URL
    private static final String URL = "https://dialogflow.googleapis.com/v2/projects/fir-chatdemo-2c129/agent/sessions/9605d0ca-1d07-b771-d105-73324155ec5a:detectIntent";
    //天气api URL
    private static final String WEATHER_URL = "https://free-api.heweather.net/s6/weather/forecast";
    //天气key
    private static final String WEATHER_KEY = "f9680f064aaf4647a7bb58ec6c8d3898";
    //Authorization
    private static final String authorization="Bearer ya29.c.Elp0Bydwn7NA6qCxMmJSuj7EjhwT9yHgbA1h741MhhQ1jTQWfaDHWh2ljBW3EXqvbzSVVJZfKrD7T78KfMQQOh4UbYDMHCC-kj3E09j2yJxCUTffcrlb1yzcHmw";

    private HashMap<String, String> map = new LinkedHashMap<>();
    String string;
    Weather weather;
    String dialogFlowDate;
    private int count=-1;
    private int TYPE_ROBOT=1;
    private int TYPE_WEATHER=2;
    private int TYPE_MSG=3;
    private int TYPE_IMG=4;
    private int TYPE_ROBOT_REPLY=5;
    private int TYPE;
    private final Handler handler=new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//         switch(msg.what) {
//             case 111: {
            // 获取Message里面的复杂数据
            Bundle date = new Bundle();
            date = msg.getData();
            DateFormat dft = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            time = date.getString("time", dft.format(new Date()));
            switch(TYPE){
                case 1:
                    //ROBOt
                    try {
                        SaveAllChatMessageToDB(null, "Robot", time, false, "在的，请问有什么可以帮助你吗？", channel_name_txt, collection_name,docId);
                        SaveChatMessageToDB("Robot", time, false, "在的，请问有什么可以帮助你吗？", collection_name, channel_name_txt, docId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    //WEATHER

                    try {
                        SaveChatMessageToDB("Robot", time, false, desc, collection_name, channel_name_txt,docId);
                        SaveAllChatMessageToDB(null,"Robot",time,false,desc,channel_name_txt,collection_name,docId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case 3:
                    //消息
                    try {
                        SaveChatMessageToDB(username, time, false, chat_msg, collection_name, channel_name_txt, docId);
                        SaveAllChatMessageToDB(null, username, time, false, chat_msg, channel_name_txt, collection_name, docId);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    //图片
                    try {
                        putImagInfotoDB(username, time, true, mDownloadUrl.toString());
                        SaveAllChatMessageToDB(mDownloadUrl.toString(), username, time, true, null, channel_name_txt, collection_name, docId);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    //机器人回复
                    try {
                        SaveChatMessageToDB("Robot", time, false, string, collection_name, channel_name_txt,docId);
                        SaveAllChatMessageToDB("Robot", username, time, false, string, channel_name_txt, collection_name,docId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

            }
            Log.e(TAG, "网络时间：" + time);
            Log.e(TAG, "本地时间：" + dft.format(new Date()));
//             }
//         }

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉TitleBar
        //设置全屏
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        intentFilter = new IntentFilter();


        /**
         * 监听网络变化；
         */
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
        collection_name = getIntent().getStringExtra(MessageConstants.CHAT_ROOM_NAME);
        channel_name_txt = getIntent().getStringExtra(MessageConstants.TXT_CHANNEL_NAME);
        docId=getIntent().getStringExtra(MessageConstants.DOC_ID);
        for(int i=0;i<30;i++) {
            conversation.add(FirebaseTextMessage.createForRemoteUser(
                    "play basketball!", System.currentTimeMillis(),"Xzp"));
            conversation.add(FirebaseTextMessage.createForLocalUser(
                    "good idea!", System.currentTimeMillis()));


        }
        initView();
        initFirestore();
        initRecyclerView();
        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        onNewIntent(getIntent());

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);

                switch (intent.getAction()) {

                    case MyUploadService.UPLOAD_COMPLETED:
                    case MyUploadService.UPLOAD_ERROR:
                        Log.e("aafafafafaf", "finished!!!!!!!");
                        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        ) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);

                        } else {
                            try {
                                onUploadResultIntent(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                }
            }
        };

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.smart_reply_txt1:
                et_msg.setText(smart_reply_txt1.getText().toString());
                break;
            case R.id.smart_reply_txt2:
                et_msg.setText(smart_reply_txt2.getText().toString());
                break;
            case R.id.smart_reply_txt3:
                et_msg.setText(smart_reply_txt3.getText().toString());
                break;
            case R.id.et_msg:

                break;
        }
    }


    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                return;
                //Toast.makeText(context, "当前网络可用", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);

            } else {
                try {
                    onUploadResultIntent(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

    }

    private void initFirestore() {
        // TODO(developer): Implement
        mFirestore = FirebaseFirestore.getInstance();
        //获得评价最高的50家餐厅
        mQuery = mFirestore.collection(collection_name)
                .orderBy(MSG_TIME, Query.Direction.ASCENDING)
        ;
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }
        mAdapter=new ChatAdapter(mQuery,this){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onDataChanged(DocumentChange documentChange) {
                super.onDataChanged(documentChange);
                chat_msg_rv.smoothScrollToPosition(getItemCount());
                ChatMessage model=documentChange.getDocument().toObject(ChatMessage.class);
                updateChannelInfo(docId,model.getMsg_txt(),model.getMsg_time(),model.isHas_img());
                if(!model.isHas_img()) {
                    if (model.getMsg_txt().contains("@Robot")&&!username.equals(model.getMsg_user())) {
                                TYPE=TYPE_ROBOT;
                                timeUtil.getNetTime(handler);
                                count = 0;

                    }
                }

                if(count==0){

                            if(!model.getMsg_user().equals("Robot")&&i<5){
                                i++;
                                if(i>1){
                                    rebot_first_reply=false;
                                }
                                getResponseText(model.getMsg_txt());
                            }

                }
                if(!username.equals(model.getMsg_user())) {
                    if(!model.isHas_img())
                    conversation.add(FirebaseTextMessage.createForRemoteUser(model.getMsg_txt(), System.currentTimeMillis(), model.getMsg_user()));
                }else {
                    if(!model.isHas_img())
                    conversation.add(FirebaseTextMessage.createForLocalUser(model.getMsg_txt(), System.currentTimeMillis()));
                }
                if(!username.equals(model.getMsg_user())&&!model.isHas_img()&&!model.getMsg_user().equals("Robot")) {
                    //
                     getSmartReplyMessage();
                }



            }
        };
        chat_msg_rv.setLayoutManager(new LinearLayoutManager(this));
        chat_msg_rv.setAdapter(mAdapter);

    }

    private void updateChannelInfo(String docIds, String msg_txt, String msg_time,boolean hasImg) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("channel_name", channel_name_txt);
        data.put("channel_time", msg_time);
        if(hasImg)
            data.put("channel_msg","图片》》");
       else
        data.put("channel_msg", msg_txt);

        db.collection("ChannelData").document(docIds)
                .set(data)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

//


    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initView() {
        SharedPreferences sp = getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
        username = sp.getString(MessageConstants.USER_NAME, "firebase_1001");

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(MessageConstants.COLLECTION_NAME, collection_name);
        editor.commit();
        smart_reply_txt1=findViewById(R.id.smart_reply_txt1);
        smart_reply_txt2=findViewById(R.id.smart_reply_txt2);

        smart_reply_txt3=findViewById(R.id.smart_reply_txt3);
        ly_smart_reply=findViewById(R.id.ly_smartreply_txt);
        et_msg = findViewById(R.id.et_msg);
        et_msg.setOnClickListener(this);
        smart_reply_txt1.setOnClickListener(this);
        smart_reply_txt2.setOnClickListener(this);

        smart_reply_txt3.setOnClickListener(this);

        iv_select_pic = findViewById(R.id.iv_select_pic);
        bt_send_msg = findViewById(R.id.bt_send_msg);
        chat_msg_rv = findViewById(R.id.chat_msg_rv);
        txt_channnel_name = findViewById(R.id.txt_channel_name);
        txt_channnel_name.setText(channel_name_txt);
        chat_msg_rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        et_msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String msg = et_msg.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    iv_select_pic.setVisibility(View.GONE);
                    bt_send_msg.setVisibility(View.VISIBLE);
                } else {
                    iv_select_pic.setVisibility(View.VISIBLE);
                    bt_send_msg.setVisibility(View.GONE);

                }
            }
        });
    }


    /**
     * 发送消息
     *
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void OnBtSendMsgClick(View view) throws IOException {
        // Read the input field and push a new instance
        chat_msg= et_msg.getText().toString();

        if (!chat_msg.equals("")) {
            TYPE=TYPE_MSG;
            timeUtil.getNetTime(handler);

            Log.e(TAG,conversation.size()+"list_size");
//            bind.setChatMessage(chatMessage);
        }
        ly_smart_reply.setVisibility(view.GONE);
        et_msg.setText("");
        HideSoftInput();

    }

    /**
     * 隐藏输入法
     */
    private void HideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        chat_msg_rv.smoothScrollToPosition(mAdapter.getItemCount());
    }

    /**
     * 将聊天信息存入FireStore
     *
     * @param username
     * @param time
     * @param msg
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SaveChatMessageToDB(String username, String time, boolean a, String msg, String collection_path,String channel_name_txt,String docid) throws IOException {
        //Log.e("name=","name="+username);
        conversation.add(FirebaseTextMessage.createForLocalUser(msg,System.currentTimeMillis()));
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(MSG_TIME, time);
        data.put(MSG_TXT, msg);
        data.put(HAS_ICON, a);
        data.put(MSG_USER, username);
        data.put(CHANNEL_NAME, channel_name_txt);
        data.put(COLLECTION_NAME, collection_path);
        data.put(DOCID,docid);
        db.collection(collection_path)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    /**
     * 存入所有聊天信息
     * @param username
     * @param time
     * @param a
     * @param msg
     * @param
     * @throws IOException
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SaveAllChatMessageToDB(String imgpath,String username, String time, boolean a, String msg ,String channel_name_txt,String collection_name,String docid) throws IOException {
        //Log.e("name=","name="+username);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(MSG_TIME, time);
        if(!TextUtils.isEmpty(imgpath)) {
            data.put(MSG_PATH, imgpath);
        }else{
            data.put(MSG_TXT,msg);
        }
        data.put(HAS_ICON, a);
        data.put(MSG_USER, username);
        data.put(CHANNEL_NAME, channel_name_txt);
        data.put(COLLECTION_NAME, collection_name);
        data.put(DOCID,docid);

        db.collection(ALLMESSAGEDATA)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    /**
     * 选择图片并发送
     *
     * @param view
     */
    public void OnSelectPicClicked(View view) {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);

        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                Log.e("data", data.getData().toString());
                String path = null;
                if (Build.VERSION.SDK_INT >= 19) {

                    path = CompressUtils.handleImageOnKitKat(data, this, MainActivity.this);

                } else {

                    path = CompressUtils.handleImageBeforeKitkat(data, this, MainActivity.this);


                }
                Log.e(TAG , "path=" + path);
                File file = CompressUtils.displayPath(path, this, MainActivity.this);
//                Log.e("fileName","fileName="+file.toString());

//                String path = null,imgPath = null;
//                Uri pt = null;
//                if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//                ) {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
//
//                } else {
                        checkPermisson();
                        if (file != null) {
                            mFileUri = Uri.fromFile(file);
                            if (mFileUri != null) {
                                uploadFromUri(mFileUri);
                            } else {
                                Log.w(TAG, "File URI is null");
                            }

                           // file.delete();
                            Log.e(TAG,file.getPath()+"filepath:");
                          CompressUtils.deleteImage(path,this);
                        }

            //    }


            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }

        }
    }
    private boolean checkPermisson() {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
            return true;
        }
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            return true;
        }
        return false;
    }

    /**
     * 上传图片
     */
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;


        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onUploadResultIntent(Intent intent) throws IOException {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        // Log.e("DownloadUrl33333",mDownloadUrl.toString());
        // Toast.makeText(MainActivity.this,"imgPath="+mDownloadUrl.toString(),Toast.LENGTH_SHORT).show();
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
        if (mDownloadUrl != null) {
            if (!TextUtils.isEmpty(mDownloadUrl.toString())) {
                Log.e("oooooooooooo", "4444444444");
                TYPE=TYPE_IMG;
                timeUtil.getNetTime(handler);

            }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void putImagInfotoDB(String username, String time, boolean b, String mDownloadUrl) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(MSG_TIME, time);
        data.put(MSG_USER, username);
        data.put(MSG_PATH, mDownloadUrl);
        data.put(HAS_ICON, b);
        data.put(CHANNEL_NAME, channel_name_txt);
        data.put(COLLECTION_NAME, collection_name);
        db.collection(collection_name)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
        // service.unBindService();

    }

    /**
     * 获取图片真实路径
     */
    public static String getRealFilePath(Activity activity, final Uri uri) {//通过uri获取文件的绝对路径
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
            Log.e("1", "data=" + data);
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
            Log.e("2", "data=" + data);
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = activity.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    Log.e("index", cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA) + "");
                    if (index > -1) {
                        data = cursor.getString(index);
                        Log.e("3", "data=" + data);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 频道返回
     */
    public void OnChannelBkClicked(View view) {
        finish();
    }
    /**
     * 获取智能回复消息
     */
    public void getSmartReplyMessage(){

        FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
        smartReply.suggestReplies(conversation)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onSuccess(SmartReplySuggestionResult result) {
                        int size=result.getSuggestions().size();
                        if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // The conversation's language isn't supported, so the
                            // the result doesn't contain any suggestions.
                            Log.e(TAG,"not supported language!");
                            //返回值为0，visible；返回值为4，invisible；返回值为8，gone。
                            if(ly_smart_reply.getVisibility()==View.VISIBLE)
                               ly_smart_reply.setVisibility(View.GONE);
                        } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            // Task completed successfully
                            // ...
                            Log.e(TAG,"get smart_reply success!");
                              int i=0;
                            for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                i++;
                                switch (i){
                                    case 1:
                                        smart_reply_txt1.setText(suggestion.getText().toString());
                                        break;
                                    case 2:
                                        smart_reply_txt2.setText(suggestion.getText().toString());
                                        break;
                                    case 3:
                                        smart_reply_txt3.setText(suggestion.getText().toString());
                                        break;
                                }
                            }

                            ly_smart_reply.setVisibility(View.VISIBLE);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Log.e(TAG,"catch a unknow error:"+e.toString());
                        ly_smart_reply.setVisibility(View.GONE);
                    }
                });



    }
    //得到dialogflow返回信息
    private void getResponseText(String msg){

        String param = "{\"queryInput\":{\"text\":{\"text\":\""+msg+"\",\"languageCode\":\"zh-cn\"}},\"queryParams\":{\"timeZone\":\"Asia/Shanghai\"}}";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, param);

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Authorization",authorization)
                    .addHeader("Content-Type","application/json;charset:utf-8")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG,"失败");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()){
                        Log.d(TAG,"成功");
                        try {
                            string = JsonParase.paraseDialogFlow(response.body().string());

                            Log.e(TAG,string+"info:");
                            Log.e(TAG,string);
                            runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                public void run() {
                                    if(string.contains("Weather")){
                                        String cityName = string.split(":")[1];
                                        dialogFlowDate = string.split(":")[2];
                                        getWeatherInfo(cityName);
                                    }else {
//                                        setShowText(string,"ROBOT");

                                        if (string == null){
                                            string = "不好意思，我听不懂这句话";
                                        }else{
                                            if(!rebot_first_reply) {
                                                   TYPE=TYPE_ROBOT_REPLY;
                                                   timeUtil.getNetTime(handler);
                                            }
                                        }

                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            response.body().close();
                        }

                    }else {
                        Log.e(TAG,"失败");
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //得到天气信息
    private void getWeatherInfo(String cityName){

        try {
            String wUrl = WEATHER_URL+"?location="+ URLEncoder.encode(cityName,"utf-8")+"&key="+WEATHER_KEY;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(wUrl)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Toast.makeText(MainActivity.this,"查询天气出错了",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if(response.isSuccessful()){
                        weather = JsonParase.paraseWeather(response.body().string());
                        runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void run() {
//                                setShowText(string,"ROBOT");
                                try {
                                    buildWeatherMsg(weather, dialogFlowDate);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void buildWeatherMsg(Weather weather, String dialogFlowDate) throws IOException {


        for (Weather.HeWeather.DailyForecast dailyForecast : weather.HeWeather6.get(0).daily_forecast){
            if (dialogFlowDate.contains(dailyForecast.date)){

                WeatherInfo weatherInfo = new WeatherInfo();

                weatherInfo.setLocation(weather.HeWeather6.get(0).basic.location);
                weatherInfo.setCond_code_d(dailyForecast.cond_code_d);
                weatherInfo.setCond_txt_d(dailyForecast.cond_txt_d);
                weatherInfo.setDate(dailyForecast.date);
                weatherInfo.setTmp_max(dailyForecast.tmp_max);
                weatherInfo.setTmp_min(dailyForecast.tmp_min);
                weatherInfo.setVis(dailyForecast.vis);
                weatherInfo.setWind_spd(dailyForecast.wind_spd);
                weatherInfo.setWind_dir(dailyForecast.wind_dir);

                String month = weatherInfo.getDate().split("-")[1];
                String day = weatherInfo.getDate().split("-")[2];
                 desc= "  "+weatherInfo.getLocation()+" "+month+"月"+day+"日 "+weatherInfo.getCond_txt_d()+","+weatherInfo.getTmp_min()+"度到"
                        +weatherInfo.getTmp_max()+"度,"+weatherInfo.getWind_dir()+",风速为"+weatherInfo.getWind_spd()+"公里/小时";
                Log.e(TAG,"weatherInfo:"+desc);
                if(!TextUtils.isEmpty(desc)) {
                    count=-1;
                    TYPE=TYPE_WEATHER;
                    timeUtil.getNetTime(handler);

                }

            }
        }


        }
    }


