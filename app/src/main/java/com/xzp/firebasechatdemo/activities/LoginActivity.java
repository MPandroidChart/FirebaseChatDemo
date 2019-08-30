package com.xzp.firebasechatdemo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.xzp.firebasechatdemo.BuildConfig;
import com.xzp.firebasechatdemo.R;
import com.xzp.firebasechatdemo.constants.MessageConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 退出登录
 * AuthUI.getInstance()
 *         .signOut(this)
 *         .addOnCompleteListener(new OnCompleteListener<Void>() {
 *             public void onComplete(@NonNull Task<Void> task) {
 *                 // ...
 *             }
 *         });
 * 删除用户
 * AuthUI.getInstance()
 *         .delete(this)
 *         .addOnCompleteListener(new OnCompleteListener<Void>() {
 *             @Override
 *             public void onComplete(@NonNull Task<Void> task) {
 *                 // ...
 *             }
 *         });
 */

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN=1;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private  FirebaseUser user;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private boolean hava_net;
    private boolean isfirst=true;
    private boolean change_ly=false,change_bk=false;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static  final  String CHANGE_LY="change_ly";
    private static final  String TAG="LoginActivity.class";
    private  FirebaseTranslator firebaseTranslator;
    private String affected_users;
    //此方法在主线程中调用，可以更新UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        intentFilter = new IntentFilter();
        /**
         * 监听网络变化；
         */
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
        /**
         * 接收邮箱验证邮件
         */
    //获得远程配置
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .setMinimumFetchIntervalInSeconds(1000)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.ly_remote_config_defaults);
        getRemoteConfig();


    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            // 处理消息时需要知道是成功的消息还是失败的消息
            switch (msg.what) {
                case 111:
                    Log.e(TAG,"zzzz"+change_ly);
                    SharedPreferences sharedPreferences = getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putBoolean(MessageConstants.CHANGE_LY,change_ly);
                    edit.putBoolean(MessageConstants.CHANGE_BK,change_bk);
                    edit.putString(MessageConstants.AFFECTED_USERS,affected_users);
                    edit.commit();
                    if(hava_net){

                        initLogin();
                    }
                    break;

            }

        }
    };

    /**
     *  获得远程配置
     */
    private void getRemoteConfig() {
        // [START fetch_config_with_callback]
        new Thread(){
            @Override
            public void run() {
                super.run();
                mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if(task.isSuccessful()){
                           // change_ly=mFirebaseRemoteConfig.getBoolean(CHANGE_LY);
                            String config_json=mFirebaseRemoteConfig.getString("config_json");

                            Log.e(TAG,config_json+"config");
                             JsonTool(config_json);

                        }else{


                        }
                        handler.sendEmptyMessage(111);
                    }

                });



            }
        }.start();


    }
    private void JsonTool(String result) {
        JSONObject obj;
        try {
            obj = new JSONObject(result);
            JSONArray array=obj.getJSONArray("config");
            for(int i=0;i<array.length();i++){
                JSONObject object=array.getJSONObject(i);
                change_bk=object.getBoolean("change_bk");
                change_ly=object.getBoolean("change_ly");
                affected_users=object.getString("affected_users");
                Log.e(TAG,"users="+affected_users);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void initLogin() {
        // Choose authentication providers

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser()!=null){
                    String userName=firebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    // Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPreferences = getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    if(TextUtils.isEmpty(userName)) {
                        userName="chat_"+getNum(1000);
                    }
                    edit.putString(MessageConstants.USER_NAME, userName);
                    edit.commit();
                    startActivity(new Intent(LoginActivity.this, ChannelActivity.class));
                    //Toast.makeText(LoginActivity.this,"欢迎"+firebaseAuth.getCurrentUser().getDisplayName(),Toast.LENGTH_SHORT).show();
                    finish();
                }else{
//                    ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
//                            .setAndroidPackageName("com.xzp.firebasechatdemo",  true,
//                           null)
//                            .setHandleCodeInApp(true) // This must be set to true
//                            .setUrl("https://xzp.page.link/ChatDemoEmailAuthentication") // This URL needs to be whitelisted
//                            .build();


                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),//邮箱登录
                            new AuthUI.IdpConfig.AnonymousBuilder().build(),//匿名登录
                            new AuthUI.IdpConfig.PhoneBuilder().build(),//手机号码登录
                            new AuthUI.IdpConfig.FacebookBuilder().build(),//Facebook登录
                            new AuthUI.IdpConfig.GoogleBuilder().build());//google登录

                  // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);


                }
            }
        };


       auth.addAuthStateListener(authStateListener);

    }
    public static int getNum(int endNum){
        if(endNum > 0){
            Random random = new Random();
            return random.nextInt(endNum);
        }
        return 0;
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        auth.addAuthStateListener(authStateListener);
//    }

    @Override
    protected void onStop() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
        super.onStop();
    }
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
             if(response!=null&&resultCode == RESULT_OK) {

                     // Successfully signed in
                 String userName=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                // Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();

                     SharedPreferences sharedPreferences = getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
                     SharedPreferences.Editor edit = sharedPreferences.edit();

                     if(TextUtils.isEmpty(userName)) {
                         userName="chat_"+getNum(1000);
                     }
                 edit.putString(MessageConstants.USER_NAME, userName);
                 edit.commit();
                 Toast.makeText(LoginActivity.this,"欢迎你，"+userName,Toast.LENGTH_SHORT).show();
                 startActivity(new Intent(this, ChannelActivity.class));
                     finish();
                     // ...

             }else{
                 //Toast.makeText(this, "0000", Toast.LENGTH_SHORT).show();
                 finish();
             }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                finish();
                //Toast.makeText(this,"登录失败！",Toast.LENGTH_SHORT).show();
            }
        }
        class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {

                //Toast.makeText(context, "当前网络可用", Toast.LENGTH_SHORT).show();

                    hava_net=true;


            } else {
                Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_login);
                hava_net=false;

            }
        }
    }


}
