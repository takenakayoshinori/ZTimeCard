package com.example.uo.timecard.other;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.example.uo.timecard.BaseActivity;
import com.example.uo.timecard.ScheduleReceiver;
import com.example.uo.timecard.ScheduleService;
import com.example.uo.timecard.common.CheckUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DailyScheduler {

    private Context context;

    public DailyScheduler(Context context) {
        this.context = context;
    }

    /*
     * duration_time(ミリ秒)後 launch_serviceを実行する
     * service_idはどのサービスかを区別する為のID(同じなら上書き)
     * 一回起動するとそのタイミングで毎日1回動き続ける
     */
    public <T> void set(Class<T> launch_service, long duration_time, int service_id, Map<String, String> launch_param) {

        Intent intent = new Intent(context, launch_service);
//        Intent intent = new Intent();
        if(launch_param != null) {
            for (String key : launch_param.keySet()) {
                intent.putExtra(key, launch_param.get(key));
            }
        }
        PendingIntent action = PendingIntent.getBroadcast(context, service_id, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //android v23以降の場合
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, duration_time, action);
        } else {
            alarm.setExact(AlarmManager.RTC_WAKEUP, duration_time, action);

//            alarm.setRepeating(AlarmManager.RTC_WAKEUP,
//                    duration_time, AlarmManager.INTERVAL_DAY, action);
        }
    }

    /*
     * 起動したい時刻(hour:minuite)を指定するバージョン
     * 指定した時刻で毎日起動する
     */
    public <T> void setByTime(Class<T> launch_service, int hour, int minuite, int service_id, Map<String, String> launch_param) {
        // 日本(+9)以外のタイムゾーンを使う時はここを変える
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");

        //今日の目標時刻のカレンダーインスタンス作成
        Calendar cal_target = Calendar.getInstance();
        cal_target.setTimeZone(tz);
        cal_target.set(Calendar.HOUR_OF_DAY, hour);
        cal_target.set(Calendar.MINUTE, minuite);
        cal_target.set(Calendar.SECOND, 0);

        //現在時刻のカレンダーインスタンス作成
        Calendar cal_now = Calendar.getInstance();
        cal_now.setTimeZone(tz);

        //ミリ秒取得
        long target_ms = cal_target.getTimeInMillis();
        long now_ms = cal_now.getTimeInMillis();

        //今日ならそのまま指定
        if (target_ms >= now_ms) {
            set(launch_service, target_ms, service_id, launch_param);
            //過ぎていたら明日の同時刻を指定
        } else {
            cal_target.add(Calendar.DAY_OF_MONTH, 1);
            target_ms = cal_target.getTimeInMillis();
            set(launch_service, target_ms, service_id, launch_param);
        }
    }

    /*
     * キャンセル用
     */
    public <T> void cancel(Class<T> launch_service, long wake_time, int service_id) {
        Intent intent = new Intent(context, launch_service);
        PendingIntent action = PendingIntent.getService(context, service_id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(action);
    }

    //出勤、退勤タイマーのセット
    public void setTimeCardTimer(){
        setStartTimer();
        setEndTimer();
    }

    //出勤タイマーのセット
    public void setStartTimer(){
        //出社設定
        Map<String, String> param = new HashMap<String, String>();
        SharedPreferences pref = context.getSharedPreferences(BaseActivity.TIME_CARD_DATA, Context.MODE_PRIVATE);
        String startTime = pref.getString(BaseActivity.KEY_START_TIME,"");

        param.put(ScheduleService.SCHEDULE_PARAM_NAME, ScheduleService.SCHEDULE_PARAM_START);
        if (!CheckUtil.isEmpty(startTime)) {
            //前の予約が残るようなので、明示的にキャンセル
            cancel(ScheduleReceiver.class, 0, ScheduleService.SERVICE_ID_TIME_CARD_START);
            int[] hourMinute = toHourMinute(startTime);
            setByTime(ScheduleReceiver.class, hourMinute[0], hourMinute[1], ScheduleService.SERVICE_ID_TIME_CARD_START, param);
        } else {
            cancel(ScheduleReceiver.class, 0, ScheduleService.SERVICE_ID_TIME_CARD_START);
        }
    }
    //退勤タイマーのセット
    public void setEndTimer(){
        //出社設定
        Map<String, String> param = new HashMap<String, String>();
        SharedPreferences pref = context.getSharedPreferences(BaseActivity.TIME_CARD_DATA, Context.MODE_PRIVATE);
        String startTime = pref.getString(BaseActivity.KEY_START_TIME,"");
        String endTime = pref.getString(BaseActivity.KEY_END_TIME,"");

        //退社設定
        param.put(ScheduleService.SCHEDULE_PARAM_NAME, ScheduleService.SCHEDULE_PARAM_END);
        if (!CheckUtil.isEmpty(endTime)) {
            //前の予約が残るようなので、明示的にキャンセル
            cancel(ScheduleReceiver.class, 0, ScheduleService.SERVICE_ID_TIME_CARD_END);

            int[] hourMinute = hourMinute = toHourMinute(endTime);
            setByTime(ScheduleReceiver.class, hourMinute[0], hourMinute[1], ScheduleService.SERVICE_ID_TIME_CARD_END, param);
        } else {
            cancel(ScheduleReceiver.class, 0, ScheduleService.SERVICE_ID_TIME_CARD_END);
        }
    }
    //時刻変換
    private static int[] toHourMinute(String hhmm){
        int[] hourMinute = new int[2];
        if(!CheckUtil.isEmpty(hhmm) && hhmm.indexOf(":") > 0){
            String[] strs = hhmm.split(":");
            try {
                hourMinute[0] = Integer.parseInt(strs[0]);
                hourMinute[1] = Integer.parseInt(strs[1]);
            } catch(Exception e){

            }
        }
        return hourMinute;
    }
}