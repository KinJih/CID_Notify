package com.cid_notify.cid_notify.Model;

import android.os.Build;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Device {

    private String model;
    private String login_Time;
    private String token;
    private String sid;

    public Device(){}
    public Device(String token,String SID){
        model = Build.MODEL;
        this.token =token;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
        login_Time = df.format(c.getTime());
        sid=SID;
    }
    public String getModel(){return model;}
    public String getLogin_Time(){return login_Time;}
    public String getToken(){return token;}
    public String getsid(){return sid;}
}
