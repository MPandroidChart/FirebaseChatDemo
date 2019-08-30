package com.xzp.firebasechatdemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.xzp.firebasechatdemo.BuildConfig;
import com.xzp.firebasechatdemo.MainActivity;
import com.xzp.firebasechatdemo.R;
import com.xzp.firebasechatdemo.constants.MessageConstants;
import com.xzp.firebasechatdemo.domain.Channel;
import com.xzp.firebasechatdemo.domain.ChatMessage;

import java.util.List;

public class ChannelAdapter extends FirestoreAdapter<ChannelAdapter.Viewholder> {
    private List<Channel>datalist;
    private Context mcontext;
    private static  final  String CHAT_DATA="ChatData";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static  final  String CHANGE_LY="change_ly";
    private static  final  String TAG="ChannelAdapter.class";
    private boolean ischange;
    private String affected_users;
    private String user_name;
    private String docId;



    public ChannelAdapter(Query query,Context context){
        super(query,context);
        mcontext=context;




    }


    @NonNull
    @Override
    public ChannelAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
             int layout;
        SharedPreferences sharedPreferences = mcontext.getSharedPreferences(MessageConstants.USER_INFO, Context.MODE_PRIVATE);
        ischange=sharedPreferences.getBoolean(MessageConstants.CHANGE_LY,false);
        affected_users=sharedPreferences.getString(MessageConstants.AFFECTED_USERS,null);
        user_name=sharedPreferences.getString(MessageConstants.USER_NAME,null);
        Log.e(TAG,"user="+affected_users);
             if(user_name.contains(affected_users)) {
                  layout = ischange ? R.layout.channel_item_change_ly : R.layout.channel_item_ly;
             }else {
                  layout=R.layout.channel_item_ly;
             }
             Log.e(TAG,ischange+"");
            return new ChannelAdapter.Viewholder(inflater.inflate(layout, viewGroup, false));

    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder viewholder, final int i) {
        viewholder.bind(getSnapshot(i));
        viewholder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Channel model=getSnapshot(i).toObject(Channel.class);
                docId=getSnapshot(i).getId();
                Intent i1=new Intent(mcontext,MainActivity.class);
                i1.putExtra(MessageConstants.CHAT_ROOM_NAME,docId);
                i1.putExtra(MessageConstants.TXT_CHANNEL_NAME,model.getChannel_name());
                i1.putExtra(MessageConstants.DOC_ID,docId);
                mcontext.startActivity(i1);
            }
        });
    }

    static class Viewholder extends RecyclerView.ViewHolder{
        TextView channel_name;
        TextView channel_time;
        TextView channel_msg;
        View view;
        ImageView chat_icon;

        public Viewholder(@NonNull View itemView) {

            super(itemView);
            channel_msg=itemView.findViewById(R.id.channel_msg);
            channel_name=itemView.findViewById(R.id.channel_name);
            channel_time=itemView.findViewById(R.id.channel_time);
            view=itemView;

        }
        public void bind(final DocumentSnapshot snapshot) {

            Channel model = snapshot.toObject(Channel.class);
            Resources resources = view.getResources();

            channel_time.setText(model.getChannel_time());
            channel_name.setText(model.getChannel_name());
            channel_msg.setText(model.getChannel_msg());
            //  Log.e("time",model.getMsg_time());


        }


    }
}
