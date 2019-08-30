package com.xzp.firebasechatdemo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.xzp.firebasechatdemo.constants.MessageConstants;

import java.util.Arrays;
import java.util.List;

public class TestLogin extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(response!=null&&resultCode == RESULT_OK) {

                // Successfully signed in

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

}
