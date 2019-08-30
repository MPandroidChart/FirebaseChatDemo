package com.xzp.firebasechatdemo.utils;

import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

public class JsonParase {

    //解析天气json
    public static Weather paraseWeather(String json){
        Gson gson = new Gson();
        Weather weather = gson.fromJson(json,Weather.class);
        if(weather != null){
            return weather;
        }
        return null;
    }



    //解析DialogFlow json
    public static String paraseDialogFlow(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject queryResult = jsonObject.optJSONObject("queryResult");
        return queryResult.optString("fulfillmentText");
    }

}
