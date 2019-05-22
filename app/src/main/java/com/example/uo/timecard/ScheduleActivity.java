package com.example.uo.timecard;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.uo.timecard.common.CheckUtil;
import com.example.uo.timecard.other.DailyScheduler;

import java.util.Calendar;

public class ScheduleActivity extends BaseActivity {

    private boolean isScheduleChecked = false;
    private boolean isAlertChecked = false;
    private boolean isBootChecked = false;

    //時刻アプリ起動テキスト
    private TextView timerCalledText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

//
//        try {
//            // logcatをクリアしておく
//
//
//            Thread.sleep(1000);
//
////            Process process = new ProcessBuilder("dumpsys","activity" ).start();
//            Process process = new ProcessBuilder("dumpsys","alarm" ).start();
//            BufferedReader bufferReader
//                    = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = bufferReader.readLine()) != null) {
//                System.out.println(line);
//            }
//
//        } catch (IOException | InterruptedException ignored) {
//            System.out.println(ignored.toString());
//
//        }
//















        //初期値設定
        TextView textSttm = findViewById(R.id.text_sttm);
        TextView textEdtm = findViewById(R.id.text_edtm);
        Switch switchSchedule = findViewById(R.id.switch_schedule);
        RadioGroup radioGroupApp = findViewById(R.id.radiogroup_act);
        Switch switchBoot = findViewById(R.id.switch_boot);
        textSttm.setText(getSaveString(KEY_START_TIME,""));
        textEdtm.setText(getSaveString(KEY_END_TIME,""));
        isScheduleChecked = getSaveBoolean(KEY_SCHEDULE_ON,false);
        switchSchedule.setChecked(isScheduleChecked);
        isBootChecked = getSaveBoolean(KEY_BOOT_ON,false);
        switchBoot.setChecked(isBootChecked);

        //ラジオボタン
        RadioButton radioApp = findViewById(R.id.radio_activity);
        if(radioApp.getText().equals(getSaveString(KEY_SCHEDULE_ACT))){
            radioGroupApp.check(radioApp.getId());
        } else {
            radioApp = findViewById(R.id.radio_notification);
            if(radioApp.getText().equals(getSaveString(KEY_SCHEDULE_ACT))){
                radioGroupApp.check(radioApp.getId());
            }
        }

        // ボタンを設定
        // API Level 26 から総称型対応となりました
        Button buttonBack = findViewById(R.id.button_back);
        Button buttonSetup = findViewById(R.id.button_scheduleSetup);

        //SWICTH ON、OFFの保持
        switchSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isScheduleChecked = isChecked;
            }
        });
        switchBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBootChecked = isChecked;
            }
        });

        //時刻アプリ起動
        View.OnClickListener timerLoadListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //前回設定時刻の取得
                timerCalledText = (TextView)view;
                String hhmm = timerCalledText.getText().toString();
                int[] hourMinute = toHourMinute(hhmm);
                int hour = hourMinute[0];
                int minute = hourMinute[1];

                TimePickerDialog dialog = new TimePickerDialog(
                    ScheduleActivity.this,
                    new TimePickerDialog.OnTimeSetListener(){
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            timerCalledText.setText(String.format("%02d:%02d", hourOfDay,minute));
                            timerCalledText = null;
                        }
                    },
                    hour,minute,true);
                dialog.show();
            }
        };
        textSttm.setOnClickListener(timerLoadListener);
        textEdtm.setOnClickListener(timerLoadListener);

        //設定ボタンクリック
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView textSttm = findViewById(R.id.text_sttm);
                TextView textEdtm = findViewById(R.id.text_edtm);
                Switch switchSchedule = findViewById(R.id.switch_schedule);
                RadioGroup radioGroupAct = findViewById(R.id.radiogroup_act);
                CheckBox checkNoTime = findViewById(R.id.check_notime);
                Switch switchBoot = findViewById(R.id.switch_boot);

                //値の保持
                setSaveString(KEY_START_TIME, textSttm.getText().toString());
                setSaveString(KEY_END_TIME, textEdtm.getText().toString());
                setSaveBoolean(KEY_SCHEDULE_ON, isScheduleChecked);

                int checkedId = radioGroupAct.getCheckedRadioButtonId();
                if (-1 != checkedId) {
                    setSaveString(KEY_SCHEDULE_ACT, ((RadioButton)findViewById(checkedId)).getText().toString());
                } else {
                    setSaveString(KEY_SCHEDULE_ACT, null);
                }
                setSaveBoolean(KEY_BOOT_ON, isBootChecked, true);

//                //!!!!!!!!!!!!!!!!!!!!!!!
//                // 時間をセットする
//                Calendar calendar = Calendar.getInstance();
//                // Calendarを使って現在の時間をミリ秒で取得
//                calendar.setTimeInMillis(System.currentTimeMillis());
//                // 5秒後に設定
//                calendar.add(Calendar.SECOND, 3);
//
//                //明示的なBroadCast
//                Intent intent = new Intent(getApplicationContext(),
//                        ScheduleReceiver.class);
//                PendingIntent pending = PendingIntent.getBroadcast(
//                        getApplicationContext(), 0, intent, 0);
//
//                // アラームをセットする
//                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//                if(am != null){
//                    am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
//
//                    Toast.makeText(getApplicationContext(),
//                            "Set Alarm ", Toast.LENGTH_SHORT).show();
//                }
//                //!!!!!!!!!!!!!!!!!!!!!!!



                //スケジュール設定
                DailyScheduler scheduler = new DailyScheduler(getApplicationContext());
                if (isScheduleChecked) {
                    //即時設定
                    if(checkNoTime.isChecked()) {
                        Intent intent = new Intent(getApplicationContext(), ScheduleReceiver.class);
                        intent.putExtra(ScheduleService.SCHEDULE_PARAM_NAME, ScheduleService.SCHEDULE_PARAM_TEST);
                        PendingIntent pending = PendingIntent.getBroadcast(
                                getApplicationContext(), ScheduleService.SERVICE_ID_TIME_CARD_NO_WAIT, intent, 0);
                        // アラームをセットする
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        am.set(AlarmManager.RTC_WAKEUP,
                                Calendar.getInstance().getTimeInMillis() + 3000, pending);
                    }
                    //出勤、退勤のタイマーセット
                    scheduler.setTimeCardTimer();
                    Toast.makeText(getApplicationContext(), "Set Alarm ", Toast.LENGTH_SHORT).show();
                } else {
                    //自動起動のクリア
                    scheduler.cancel(ScheduleReceiver.class, 0, ScheduleService.SERVICE_ID_TIME_CARD_START);
                    scheduler.cancel(ScheduleReceiver.class, 0, ScheduleService.SERVICE_ID_TIME_CARD_END);
                    Toast.makeText(getApplicationContext(), "Clear Alarm ", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        //戻りボタン
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
