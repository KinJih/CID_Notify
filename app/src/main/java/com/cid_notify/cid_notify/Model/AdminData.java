package com.cid_notify.cid_notify.Model;

import android.support.annotation.NonNull;

import com.cid_notify.cid_notify.Util.EncryptUtil;

public class AdminData {

    private String secondPassword;
    private String cellphone;
    private String birthday;

    public AdminData(){}

    public AdminData(String secondPassword,String cellphone,String birthday){
        this.birthday=birthday;
        this.cellphone=cellphone;
        this.secondPassword=EncryptUtil.pwd2sha(secondPassword,cellphone,birthday);
    }


    public String getsecondPassword() {
        return secondPassword;
    }

    public String getbirthday() {
        return birthday;
    }

    public String getcellphone() {
        return cellphone;
    }
}
