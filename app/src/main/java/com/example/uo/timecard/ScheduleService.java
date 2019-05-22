package com.example.uo.timecard;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.uo.timecard.common.CheckUtil;
import com.example.uo.timecard.common.DateUtil;
import com.example.uo.timecard.common.TimeInfo;
import com.example.uo.timecard.other.DailyScheduler;

public class ScheduleService extends IntentService {

    public static final String SCHEDULE_PARAM_NAME = "schedule_param";
    public static final String SCHEDULE_PARAM_START = "1";
    public static final String SCHEDULE_PARAM_END = "2";
    public static final String SCHEDULE_PARAM_TEST = "3";

    //タイムカード自動起動サービスID
    public static final int SERVICE_ID_TIME_CARD_START = 10001;
    public static final int SERVICE_ID_TIME_CARD_END = 10002;
    public static final int SERVICE_ID_TIME_CARD_NO_WAIT = 10003;

    public static final String CANNEL_ID = "10002";

    private String actKbn = null;

    public ScheduleService() {
        super("ScheduleService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // to do something
        actKbn = intent.getStringExtra(SCHEDULE_PARAM_NAME);

        // ForegroundにするためNotificationが必要(android8以降対応)
        String channelName = "TimeCard";
        NotificationCompat.Builder builder = null;
        //Android8.1以降、独自チャネルの作成が必要(分岐は8.0以降)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "timecard.channel";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = builder.setOngoing(true)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), "default");
        }
        // サービス永続化
        startForeground(1001, builder.build());

//        CheckUtil.debugToast(getApplicationContext(),"called Service2");

        if(!SCHEDULE_PARAM_TEST.equals(actKbn)) {
            CheckUtil.debugToast(getApplicationContext(),"set next Schedule");

//            // 日本(+9)以外のタイムゾーンを使う時はここを変える
//            TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
//
//            Calendar cal_target = Calendar.getInstance();
//            cal_target.setTimeZone(tz);
//            long target_ms = cal_target.getTimeInMillis() + 24 * 60 * 60 * 1000 - 1000;
//            Map<String, String> param = new HashMap<String, String>();
//            param.put(ScheduleService.SCHEDULE_PARAM_NAME, actKbn);

            DailyScheduler scheduler = new DailyScheduler(getApplicationContext());
            if (SCHEDULE_PARAM_START.equals(actKbn)) {
                //出社設定
                scheduler.setStartTimer();
            } else if (SCHEDULE_PARAM_END.equals(actKbn)) {
                //退社設定
                scheduler.setEndTimer();
            }
        }
        //営業日でない場合、処理終了
        if(!SCHEDULE_PARAM_TEST.equals(actKbn)) {
            if (!DateUtil.checkToday()) {
                CheckUtil.debugToast(getApplicationContext(),"holiday");

                stopSelf();
                return;
            }
        }
        SharedPreferences pref = getSharedPreferences(BaseActivity.TIME_CARD_DATA, Context.MODE_PRIVATE);
        String userId = pref.getString(BaseActivity.KEY_USER_ID,"");
        String password = pref.getString(BaseActivity.KEY_PASSWORD,"");

        //タイムカード情報の取得
        TimeCardAction action = new TimeCardAction(userId, password);
        action.setAsyncTaskCallbackListener(new AsyncTaskCallbackListener() {
            public void onPostExecute(TimeInfo timeInfo) {
                if(SCHEDULE_PARAM_TEST.equals(actKbn) || checkAlert(timeInfo)){

                    CheckUtil.debugToast(getApplicationContext(),"call Timecard");

                    //タイムカードを起動する
                    callApp(timeInfo);
                }
                stopSelf();
            }
        });
        action.execute(TimeCardAction.ACTION_TYPE_SHOW);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // タイマー起動判定
    // true:起動 false:キャンセル
    protected boolean checkAlert(TimeInfo timeInfo){

        //出社済みチェック(出社済みの場合、処理終了)
        if(SCHEDULE_PARAM_START.equals(actKbn)){
            if(timeInfo.dispStartBtn){
                return true;
            }
        } else if(SCHEDULE_PARAM_END.equals(actKbn)) {
            if(timeInfo.dispEndBtn){
                return true;
            }
        }
        return false;
    }
    //タイムカードを起動する
    protected void callApp(TimeInfo timeInfo){
        SharedPreferences pref = getSharedPreferences(BaseActivity.TIME_CARD_DATA, Context.MODE_PRIVATE);
        String actName = pref.getString(BaseActivity.KEY_SCHEDULE_ACT,"");

        CheckUtil.debugToast(getApplicationContext(),"called Timecard");

        if(getString(R.string.select_activity).equals(actName)) {

            CheckUtil.debugToast(getApplicationContext(),"set actApp");

            //アプリ起動
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(MainActivity.PARAM_TIME_INFO, timeInfo);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {500, 500, 500, 500}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if(getString(R.string.select_notification).equals(actName)) {

            CheckUtil.debugToast(getApplicationContext(),"set notification");

            //Version26以上
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                String name = "タイムカードスケジュール"; // 通知チャンネル名
                int importance = NotificationManager.IMPORTANCE_HIGH; // デフォルトの重要度
                NotificationChannel channel = new NotificationChannel(CANNEL_ID, name, importance);

                // 通知チャンネルの設定のデフォルト値。設定必須ではなく、ユーザーが変更可能。
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                channel.enableVibration(true);
                channel.enableLights(true);

                // NotificationManagerCompatにcreateNotificationChannel()は無い。
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.createNotificationChannel(channel);
            }

            //通知を表示
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CANNEL_ID);
            builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
            builder.setContentTitle(getString(R.string.notify_title));
            builder.setContentText(getString(R.string.notify_text));

            // クリックでメイン画面起動
            Intent intentActivity = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intentActivity, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(contentIntent);

            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            builder.setAutoCancel(true);

            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(0, builder.build());
        }
    }
}
