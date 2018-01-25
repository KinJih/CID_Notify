package com.cid_notify.cid_notify.Util;

import android.util.Log;

import java.security.MessageDigest;

public class EncryptUtil {

    public static String pwd2sha(String pwd,String cell,String birth){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            Log.d("PWD",pwd);
            messageDigest.update((pwd + cell + birth).getBytes());
            byte[] bytes = messageDigest.digest();
            return byte2String(bytes);
        }catch (Exception e){
            return e.toString();
        }
    }

    private static String byte2String(byte[] bytes){
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }
}
