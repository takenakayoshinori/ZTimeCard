package com.example.uo.timecard;

import android.os.AsyncTask;

import com.example.uo.timecard.common.TimeInfo;
import com.example.uo.timecard.network.TimeCard;

public class TimeCardAction  extends AsyncTask<Integer, Integer, TimeInfo> {
    private AsyncTaskCallbackListener listener = null;
    private boolean flag = false;

    private String userId = null;
    private String passwd = null;

    //表示
    public static final int ACTION_TYPE_SHOW = 0;
    //出社
    public static final int ACTION_TYPE_START = 1;
    //退社
    public static final int ACTION_TYPE_END = 2;

    TimeCardAction(String userId, String passwd){
        this.userId = userId;
        this.passwd = passwd;
    }

    public void setAsyncTaskCallbackListener(AsyncTaskCallbackListener listener) {
        this.listener = listener;
    }

    /**
     * バックグランドで行う処理
     */
    @Override
    protected TimeInfo doInBackground(Integer... value) {
        TimeCard timeCard = TimeCard.getInstance(userId, passwd);
        if(value[0].intValue() == ACTION_TYPE_START){
            timeCard.syusya();
            timeCard = TimeCard.getInstance(userId, passwd);
        } else if(value[0].intValue() == ACTION_TYPE_END){
            timeCard.taisya();
            timeCard = TimeCard.getInstance(userId, passwd);
        }
        return timeCard.getTimeInfo();
    }

    /**
     * バックグランド処理が完了し、UIスレッドに反映する
     */
    @Override
    protected void onPostExecute(TimeInfo timeInfo) {
        // ここでリスナーのコールバックを呼び出す
        if (listener != null) {
            listener.onPostExecute(timeInfo);
        }
    }
}
