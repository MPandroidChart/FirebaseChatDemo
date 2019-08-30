//package com.xzp.firebasechatdemo.utils;
//
//import android.util.Log;
//import android.widget.Toast;
//
//import com.squareup.okhttp.Call;
//import com.squareup.okhttp.Callback;
//import com.squareup.okhttp.MediaType;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//
//public class QueryWeatherUtil {
//    //得到dialogflow返回信息
//    private void getResponseText(String msg,String URL){
//
//        String param = "{\"queryInput\":{\"text\":{\"text\":\""+msg+"\",\"languageCode\":\"zh-cn\"}},\"queryParams\":{\"timeZone\":\"Asia/Shanghai\"}}";
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
//
//        try {
//            OkHttpClient client = new OkHttpClient();
//            RequestBody body = RequestBody.create(JSON, param);
//
////            MultipartBody multipartBody = new MultipartBody.Builder()
////                    .setType(MultipartBody.FORM)
////                    .addFormDataPart("params",body)
////                    .build();
//
//            Request request = new Request.Builder()
//                    .url(URL)
//                    .addHeader("Authorization","Bearer ya29.c.ElpqB0bCt7sD8WdLbw2P5UmDPRCJ0g4_PmwL9qnr7Jtz0uwrdMgILKs5WUl2fAu8o_BTZNn5PMFobxbBOkCBEmsBBKU4psw11lFNfQNuTdAXaynVmCfVcj6vYuo")
//                    .addHeader("Content-Type","application/json;charset:utf-8")
//                    .post(body)
//                    .build();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Request request, IOException e) {
//                    Log.d("MainMain","失败");
//                }
//
//                @Override
//                public void onResponse(Response response) throws IOException {
//                    if (response.isSuccessful()){
//                        Log.d("MainMain","成功");
//                        try {
//                       final   String    string = JsonParase.paraseDialogFlow(response.body().string());
//                            Log.d("MainMain",string);
//                            new Thread(new Runnable() {
//                                public void run() {
//                                    if(string.contains("Weather")){
//                                        String cityName = string.split(":")[1];
//                                     String    dialogFlowDate = string.split(":")[2];
//                                        getWeatherInfo(cityName);
//                                    }else {
////                                        setShowText(string,"ROBOT");
//
//                                        if (mStt.isSpeaking()){
//
//                                        if (string == null){
//                                            string = "不好意思，我听不懂这句话";
//                                        }
//
//                                        Message message = new Message("ROBOT",string,Message.Type.ROBOT_NORMAL);
//                                        messageList.add(message);
//                                        adapter.notifyDataSetChanged();
//                                        chatRecycler.smoothScrollToPosition(adapter.getItemCount()-1);
//
//                                        SpeechUtil.setSynParam(mStt);
//                                        mStt.startSpeaking(message.getMsg(),SpeechUtil.mSynthesizerListener);
//                                    }
//                                }
//                            }).start();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }finally {
//                            response.body().close();
//                        }
//
//                    }else {
//                        Log.d("MainMain","失败");
//                    }
//                }
//
//
//
//
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//    //得到天气信息
//    private void getWeatherInfo(String cityName){
//
//        try {
//            String wUrl = WEATHER_URL+"?location="+ URLEncoder.encode(cityName,"utf-8")+"&key="+WEATHER_KEY;
//
//            OkHttpClient client = new OkHttpClient();
//
//            Request request = new Request.Builder()
//                    .url(wUrl)
//                    .build();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Toast.makeText(MainActivity.this,"查询天气出错了",Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if(response.isSuccessful()){
//                        weather = JsonParase.paraseWeather(response.body().string());
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
////                                setShowText(string,"ROBOT");
//                                buildWeatherMsg(weather, dialogFlowDate);
//                            }
//                        });
//                    }
//                }
//            });
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    private void buildWeatherMsg(Weather weather, String dialogFlowDate){
//
//        boolean hasDate = false;
//
//        for (Weather.HeWeather.DailyForecast dailyForecast : weather.HeWeather6.get(0).daily_forecast){
//            if (dialogFlowDate.contains(dailyForecast.date)){
//
//                WeatherInfo weatherInfo = new WeatherInfo();
//
//                weatherInfo.setLocation(weather.HeWeather6.get(0).basic.location);
//                weatherInfo.setCond_code_d(dailyForecast.cond_code_d);
//                weatherInfo.setCond_txt_d(dailyForecast.cond_txt_d);
//                weatherInfo.setDate(dailyForecast.date);
//                weatherInfo.setTmp_max(dailyForecast.tmp_max);
//                weatherInfo.setTmp_min(dailyForecast.tmp_min);
//                weatherInfo.setVis(dailyForecast.vis);
//                weatherInfo.setWind_spd(dailyForecast.wind_spd);
//                weatherInfo.setWind_dir(dailyForecast.wind_dir);
//
//                if (mStt.isSpeaking()){
//                    mStt.stopSpeaking();
//                }
//
//                Message message = new Message("ROBOT","",Message.Type.ROBOT_WEATHER);
//                message.setWeatherInfo(weatherInfo);
//
//                messageList.add(message);
//                adapter.notifyDataSetChanged();
//                chatRecycler.smoothScrollToPosition(adapter.getItemCount()-1);
//
//                String month = weatherInfo.getDate().split("-")[1];
//                String day = weatherInfo.getDate().split("-")[2];
//                String desc = "  "+weatherInfo.getLocation()+" "+month+"月"+day+"日 "+weatherInfo.getCond_txt_d()+","+weatherInfo.getTmp_min()+"度到"
//                        +weatherInfo.getTmp_max()+"度,"+weatherInfo.getWind_dir()+",风速为"+weatherInfo.getWind_spd()+"公里/小时";
//
//                SpeechUtil.setSynParam(mStt);
//                mStt.startSpeaking(desc,SpeechUtil.mSynthesizerListener);
//
//                hasDate = true;
//            }
//        }
//
//        if (hasDate == false){
//
//            if (mStt.isSpeaking()){
//                mStt.stopSpeaking();
//            }
//
//            Message message = new Message("ROBOT","您只能查询最近三天的天气哦！",Message.Type.ROBOT_NORMAL);
//            messageList.add(message);
//            adapter.notifyDataSetChanged();
//            chatRecycler.smoothScrollToPosition(adapter.getItemCount()-1);
//
//            SpeechUtil.setSynParam(mStt);
//            mStt.startSpeaking(message.getMsg(),SpeechUtil.mSynthesizerListener);
//        }
//    }
//}
