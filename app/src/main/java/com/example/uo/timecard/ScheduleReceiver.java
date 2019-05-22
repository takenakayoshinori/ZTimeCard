package com.example.uo.timecard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.example.uo.timecard.common.CheckUtil;
import com.example.uo.timecard.other.DailyScheduler;

public class ScheduleReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent){
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences pref = context.getSharedPreferences(BaseActivity.TIME_CARD_DATA, Context.MODE_PRIVATE);
            boolean isBoot = pref.getBoolean(BaseActivity.KEY_BOOT_ON, false);

            if(isBoot) {
                //出勤、退勤のタイマーセット
                DailyScheduler scheduler = new DailyScheduler(context);
                scheduler.setTimeCardTimer();
                Toast.makeText(context, "Set Alarm ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "called Alarm", Toast.LENGTH_SHORT).show();

            //サービス起動
            Intent intentService = new Intent(context, ScheduleService.class);
            intentService.putExtras(intent.getExtras());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                CheckUtil.debugToast(context,"do startForegroundService");

                //android v26以降の場合
                context.startForegroundService(intentService);
            } else {
                CheckUtil.debugToast(context,"do startService");

                context.startService(intentService);
            }

        }
    }
}
