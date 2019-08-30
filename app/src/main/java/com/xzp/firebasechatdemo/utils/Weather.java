package com.xzp.firebasechatdemo.utils;

import java.util.ArrayList;

public class Weather {

   public  ArrayList<HeWeather> HeWeather6;

    public class HeWeather {

        public  Basic basic;
        public  ArrayList<DailyForecast> daily_forecast;

        public class Basic {

            public String location;
        }

        public class DailyForecast{

            public  int cond_code_d;
            public  String cond_txt_d;
            public String date;
            public String wind_dir;
            public String wind_spd;
            public String vis;
            public String tmp_max;
            public String tmp_min;
        }
    }
}
