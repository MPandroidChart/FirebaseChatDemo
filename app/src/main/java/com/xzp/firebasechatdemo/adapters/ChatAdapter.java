package com.xzp.firebasechatdemo.adapters;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.xzp.firebasechatdemo.R;
import com.xzp.firebasechatdemo.constants.MessageConstants;
import com.xzp.firebasechatdemo.domain.ChatMessage;

import java.util.Locale;

public class ChatAdapter extends FirestoreAdapter<ChatAdapter.Viewholder> {
    private Context mcontext;
    private Application mapplication;
    private boolean beginTranslate=false;
    private final FirebaseTranslator chineseEnglishTranslator;
    private static final String TAG="ChatAdapter.class";
    private final SharedPreferences sp;
    private boolean has_translate_model;
//    final Handler handler=new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case 11:
//                    Log.e(TAG,"begin traslate!");
//                    beginTranslate=true;
//                    break;
//            }
//        }
//    };





    public ChatAdapter(Query query, Context context) {
        super(query, context);
        mcontext = context;
        // Create an English-German translator:
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.ZH)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
         chineseEnglishTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
         sp=mcontext.getSharedPreferences(MessageConstants.USER_INFO,Context.MODE_PRIVATE);
         has_translate_model=sp.getBoolean(MessageConstants.HAS_TRANSLATE_MODEL,false);
        if(!has_translate_model) {

            new Thread() {

                @Override
                public void run() {
                    super.run();
                    Log.e(TAG, "开始下载模型！");
                    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                            .requireWifi()
                            .build();
                    chineseEnglishTranslator.downloadModelIfNeeded(conditions)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void v) {
                                            // Model downloaded successfully. Okay to start translating.
                                            // (Set a flag, unhide the translation UI, etc.)
                                            //handler.sendEmptyMessage(11);

                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putBoolean(MessageConstants.HAS_TRANSLATE_MODEL, true).commit();
                                            // Toast.makeText(mcontext,"翻译模型下载成功！",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Model couldn’t be downloaded or other internal error.
                                            // ...
                                            //Toast.makeText(mcontext,"翻译模型下载失败！",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                }
            }.start();
        }



    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new Viewholder(inflater.inflate(R.layout.message_item_ly, viewGroup, false), mcontext);
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder viewholder, final int i) {
        viewholder.bind(getSnapshot(i));

        viewholder.view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if(sp.getBoolean(MessageConstants.HAS_TRANSLATE_MODEL,false)&&!getSnapshot(i).toObject(ChatMessage.class).isHas_img()) {
                  Task<String>task=chineseEnglishTranslator.translate(getSnapshot(i).toObject(ChatMessage.class).getMsg_txt());
                  task.addOnCompleteListener(new OnCompleteListener<String>() {
                      @Override
                      public void onComplete( Task<String> task) {
                          Toast.makeText(mcontext,task.getResult().toString(),Toast.LENGTH_SHORT).show();
                      }
                  });
                }
                return true;
            }
        });

    }




    static class Viewholder extends RecyclerView.ViewHolder {
        TextView chat_name;
        TextView chat_time;
        TextView chat_msg;
        ImageView chat_img;
        View view;
        Context context;
        ProgressBar pb;


        public Viewholder(@NonNull View itemView, Context mcontext) {

            super(itemView);
            chat_msg = itemView.findViewById(R.id.message_text);
            chat_name = itemView.findViewById(R.id.message_user);
            chat_time = itemView.findViewById(R.id.message_time);
            chat_img = itemView.findViewById(R.id.message_img);
            pb = itemView.findViewById(R.id.message_pb);
            view = itemView;
            context = mcontext;


        }

        public void bind(final DocumentSnapshot snapshot) {

            ChatMessage model = snapshot.toObject(ChatMessage.class);
            Resources resources = view.getResources();
            boolean has_icon = model.isHas_img();
            chat_time.setText(model.getMsg_time());

            chat_name.setText(model.getMsg_user());
            //  Log.e("time",model.getMsg_time());
            if (has_icon) {
                chat_msg.setVisibility(view.GONE);
                chat_img.setVisibility(view.VISIBLE);
                Glide.with(context).load(model.getMsg_path()).into(chat_img);
                pb.setVisibility(view.GONE);

            } else {
                chat_msg.setText(model.getMsg_txt());
                chat_img.setVisibility(view.GONE);
                pb.setVisibility(view.GONE);

            }

        }

    }
     public class Language implements Comparable<Language> {
        public  String code;

        Language(String code) {
            this.code = code;
        }

      public   String getDisplayName() {
            return new Locale(code).getDisplayName();
        }

       public  String getCode() {
            return code;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Language)) {
                return false;
            }

            Language otherLang = (Language) o;
            return otherLang.code.equals(code);
        }

        @NonNull
        public String toString() {
            return code + " - " + getDisplayName();
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public int compareTo(@NonNull Language o) {
            return this.getDisplayName().compareTo(o.getDisplayName());
        }
    }

}
