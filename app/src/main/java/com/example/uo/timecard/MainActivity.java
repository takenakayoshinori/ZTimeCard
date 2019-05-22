package com.example.uo.timecard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.uo.timecard.common.CheckUtil;
import com.example.uo.timecard.common.TimeInfo;

public class MainActivity extends BaseActivity {

    //起動パラメータ：時刻情報
    public static final String PARAM_TIME_INFO = "timeInfo";
    //処理結果：再読み込み
    public static final int RELOAD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ボタンを設定
        // API Level 26 から総称型対応となりました
        Button buttonStart = findViewById(R.id.button_start);
        Button buttonEnd = findViewById(R.id.button_end);
        Button buttonLogin = findViewById(R.id.button_login);
        Button buttonSchedule = findViewById(R.id.button_schedule);
        TextView linkCybozu = (TextView)findViewById(R.id.link_cybozu);
        buttonStart.setVisibility(View.GONE);
        buttonEnd.setVisibility(View.GONE);

        //ログイン設定
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), InputUserActivity.class);
                startActivityForResult(intent,0);
            }
        });
        //自動起動設定
        buttonSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), ScheduleActivity.class);
                startActivity(intent);
            }
        });
        //出社処理
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionExec(TimeCardAction.ACTION_TYPE_START);
            }
        });
        //退社処理
        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionExec(TimeCardAction.ACTION_TYPE_END);
            }
        });
        linkCybozu.setText("サイボーズ：https://zexis.jp/ad/ptw/ag.cgi");

        //エラー処理
        if(CheckUtil.isEmpty(getSaveString(KEY_USER_ID))){
            showError(getString(R.string.error_msg1));
            return;
        }
        //データ取得
        Intent intent = getIntent();
        if(intent.getSerializableExtra(PARAM_TIME_INFO) != null) {
            //パラメータの時刻をセット
            dispNew((TimeInfo) intent.getSerializableExtra(PARAM_TIME_INFO));
        } else {
            //時刻の取得
            actionExec(TimeCardAction.ACTION_TYPE_SHOW);
        }
    }

    // 時刻の表示
    protected void dispNew(TimeInfo timeInfo){
        TextView textStartTime = findViewById(R.id.text_startTime);
        TextView textEndTime = findViewById(R.id.text_endTime);
        Button buttonStart = findViewById(R.id.button_start);
        Button buttonEnd  = findViewById(R.id.button_end);

        //エラーの場合
        if(!CheckUtil.isEmpty(timeInfo.errorMsg)){
            TextView textMessage = findViewById(R.id.text_message);
            showError(timeInfo.errorMsg);
            return;
        }
        if(timeInfo.dispStartBtn){
            buttonStart.setVisibility(View.VISIBLE);
        } else {
            buttonStart.setVisibility(View.GONE);
        }
        textStartTime.setText(timeInfo.startTime);

        if(timeInfo.dispEndBtn){
            buttonEnd.setVisibility(View.VISIBLE);
        } else {
            buttonEnd.setVisibility(View.GONE);
        }
        textEndTime.setText(timeInfo.endTime);
    }

    //別画面からの戻り処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        showError("");
        if (resultCode == RELOAD){
            //時刻読込
            actionExec(TimeCardAction.ACTION_TYPE_SHOW);
        }
    }

    //アクションデータ取得処理
    public void actionExec(int actKbn) {
        TimeCardAction action = createAction();
        action.setAsyncTaskCallbackListener(new AsyncTaskCallbackListener() {
            public void onPostExecute(TimeInfo timeInfo) {
                if(CheckUtil.isEmpty(timeInfo.errorMsg)
                        && !timeInfo.dispStartBtn && !timeInfo.dispStartBtn
                        && CheckUtil.isEmpty(timeInfo.startTime)
                        && CheckUtil.isEmpty(timeInfo.endTime)) {
                    //値が取得できない場合、エラーメッセージを追加
                    timeInfo.errorMsg = getString(R.string.error_msg2);
                }
                dispNew(timeInfo);
            }
        });
        action.execute(actKbn);
    }

    //アクション作成
    private TimeCardAction createAction(){
        return new TimeCardAction(getSaveString(KEY_USER_ID), getSaveString(KEY_PASSWORD));
    }
    //エラー時の処理
    private void showError(String msg){
        msg = msg.replaceAll("。","。\n");
        TextView textMessage = findViewById(R.id.text_message);
        textMessage.setText(msg);
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
