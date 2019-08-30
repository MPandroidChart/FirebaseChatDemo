package com.xzp.firebasechatdemo.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.HttpMetric;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.xzp.firebasechatdemo.BuildConfig;
import com.xzp.firebasechatdemo.MainActivity;
import com.xzp.firebasechatdemo.R;
import com.xzp.firebasechatdemo.adapters.ChannelAdapter;
import com.xzp.firebasechatdemo.adapters.ChatAdapter;
import com.xzp.firebasechatdemo.constants.MessageConstants;
import com.xzp.firebasechatdemo.domain.Channel;
import com.xzp.firebasechatdemo.domain.ChatMessage;
import com.xzp.firebasechatdemo.services.ChatService;
import com.xzp.firebasechatdemo.utils.GetNetTimeUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelActivity extends AppCompatActivity  {
    private RecyclerView rv_channel;
    private ChannelAdapter adapter;
    private List<Channel>datalist;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String CHANNEL_TIME="channel_time";
    private static final String CHANNEL_MSG="channel_msg";
    private static final String CHANNEL_NAME="channel_name";
    private static final String TAG="ChannelActivity";
    private static final String CHANNEL_DATA="ChannelData";
    private static final String CHANGE_BK="change_bk";
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private ChannelAdapter mAdapter;
    private static final int LIMIT = 50;
    private AlertDialog.Builder builder;
    private Intent intent;
    private LinearLayout ly_channel_list;
    private GetNetTimeUtil timeUtil=new GetNetTimeUtil();
    private String time;
    private String   channelName;

 //   private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Handler handler=new Handler(){
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
                 putChannelDataToDB(channelName, time, "");
                 Log.e(TAG, "网络时间：" + time);
                 Log.e(TAG, "本地时间：" + dft.format(new Date()));
//             }
//         }

     }
 };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        initView();
        initFirestore();
        initRecyclerView();
        intent =new Intent(this, ChatService.class);
        startService(intent);


    }



    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
    private void initFirestore() {
        // TODO(developer): Implement
        mFirestore = FirebaseFirestore.getInstance();
        //获得评价最高的50家餐厅
        mQuery = mFirestore.collection(CHANNEL_DATA)
                .orderBy(CHANNEL_TIME, Query.Direction.DESCENDING)
                ;

    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new ChannelAdapter(mQuery,this) {

            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onDataChanged(DocumentChange change)  {
                super.onDataChanged(change);
                // Show/hide content if the query returns empty.
                String docId=change.getDocument().getReference().getId();
                Log.e(TAG,docId+"docId:");
                if (getItemCount() == 0) {
                    rv_channel.setVisibility(View.GONE);

                } else {
                    rv_channel.setVisibility(View.VISIBLE);

                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors

            }
        };

        rv_channel.setLayoutManager(new LinearLayoutManager(this));
        rv_channel.setAdapter(mAdapter);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();


        }
    }



    private void putChannelDataToDB(String channel_name,String time,String msg) {
        Map<String,Object> data=new HashMap<String,Object>();

        data.put(CHANNEL_NAME,channel_name);
        data.put(CHANNEL_TIME,time);
        data.put(CHANNEL_MSG,msg);
        db.collection(CHANNEL_DATA)
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

    private void initView() {
        rv_channel=findViewById(R.id.rv_channel);
        rv_channel.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        ly_channel_list=findViewById(R.id.ly_channel_list);
        SharedPreferences sp=getSharedPreferences(MessageConstants.USER_INFO,Context.MODE_PRIVATE);
        boolean change_ly=sp.getBoolean(MessageConstants.CHANGE_BK,false);
        if(change_ly){
                               ly_channel_list.setBackgroundColor(Color.GRAY);
                     }else{
            ly_channel_list.setBackgroundColor(Color.WHITE);
        }

//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .setMinimumFetchIntervalInSeconds(1000)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettings(configSettings);
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
//        getRemoteConfig();


    }

//    /**
//     *  获得远程配置
//     */
//    private void getRemoteConfig() {
//        // [START fetch_config_with_callback]
//                mFirebaseRemoteConfig.fetchAndActivate()
//                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Boolean> task) {
//                        if (task.isSuccessful()) {
//                            boolean updated = task.getResult();
//                            Log.d(TAG, "Config params updated: " + updated);
//                           boolean ischange=mFirebaseRemoteConfig.getBoolean(CHANGE_BK);
//                           if(ischange){
//                               ly_channel_list.setBackgroundColor(Color.GRAY);
//                           }
//
//                        } else {
//
//                        }
//
//                    }
//                });
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(ChannelActivity.this, LoginActivity.class));
                                finish();
                            }
                        });
                break;
            case R.id.menu_delete_account:
                AuthUI.getInstance()
                        .delete(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(ChannelActivity.this, LoginActivity.class));
                                finish();

                            }
                        });
                break;
            case R.id.menu_add_channel:
                           showInput();
                break;
        }
        return true;
    }
    private void showInput() {
        final EditText editText = new EditText(this);
        builder = new AlertDialog.Builder(this).setTitle("请输入频道名称").setView(editText)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        channelName=editText.getText().toString();
                        String msg="";

                        if(!TextUtils.isEmpty(channelName)) {
                            timeUtil.getNetTime(handler);

                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }


}
