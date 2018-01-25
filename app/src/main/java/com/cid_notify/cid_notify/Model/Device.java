package com.cid_notify.cid_notify.Model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Device {

    private String SID;
    private String Model;
    private String Last_Login;
    private String Token;
    public Device(){

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
    }
}
