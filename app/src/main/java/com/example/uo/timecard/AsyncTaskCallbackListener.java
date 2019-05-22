package com.example.uo.timecard;

import com.example.uo.timecard.common.TimeInfo;

public interface AsyncTaskCallbackListener {
    public void onPostExecute(TimeInfo timeInfo);
}
