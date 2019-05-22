package com.example.uo.timecard.common;

import android.content.Context;
import android.widget.Toast;

public class CheckUtil {

    //空判定
    public static boolean isEmpty(String val){
        if(val == null || "".equals(val)){
            return true;
        } else {
            return false;
        }
    }

    public static void debugToast(Context context, String msg){
        try {
            if(false){
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                Thread.sleep(1000);
            }
        } catch(Exception e){
        }
    }
}
