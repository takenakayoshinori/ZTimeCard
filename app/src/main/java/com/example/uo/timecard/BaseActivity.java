package com.example.uo.timecard;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity  extends AppCompatActivity {
    /** 保持ファイル */
    public static final String TIME_CARD_DATA = "timeCardData";

    /** ユーザーID */
    public static final String KEY_USER_ID = "userId";
    /** パスワード */
    public static final String KEY_PASSWORD = "timeCardData";

    /** 始業時刻 */
    public static final String KEY_START_TIME = "sttm";
    /** 終業時刻 */
    public static final String KEY_END_TIME = "edtm";
    /** スケジュール機能の使用 */
    public static final String KEY_SCHEDULE_ON = "scheduleFlg";
    /** 再起動時にスケジュール登録を設定 */
    public static final String KEY_BOOT_ON = "bootFlg";
    /** スケジュール起動時の動き */
    public static final String KEY_SCHEDULE_ACT = "scheduleAct";

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    //保持した値の取得
    protected String getSaveString(String key) {
        return getSaveString(key, "");
    }
    protected String getSaveString(String key, String defo) {
        if (pref == null) {
            pref = getSharedPreferences(TIME_CARD_DATA, MODE_PRIVATE);
            editor = pref.edit();
        }
        return pref.getString(key, defo);
    }

    //値の保持
    protected void setSaveString(String key, String val, boolean isCommit){
        if (pref == null) {
            pref = getSharedPreferences(TIME_CARD_DATA, MODE_PRIVATE);
            editor = pref.edit();
        }
        editor.putString(key, val);
        if(isCommit){
            editor.commit();
        }
    }
    //値の保持
    protected void setSaveString(String key, String val){
        setSaveString(key, val, false);
    }

    //保持した値の取得
    protected boolean getSaveBoolean(String key) {
        return getSaveBoolean(key, false);
    }
    protected boolean getSaveBoolean(String key, boolean defo) {
        if (pref == null) {
            pref = getSharedPreferences(TIME_CARD_DATA, MODE_PRIVATE);
            editor = pref.edit();
        }
        return pref.getBoolean(key, defo);
    }

    //値の保持
    protected void setSaveBoolean(String key, boolean val, boolean isCommit){
        if (pref == null) {
            pref = getSharedPreferences(TIME_CARD_DATA, MODE_PRIVATE);
            editor = pref.edit();
        }
        editor.putBoolean(key, val);
        if(isCommit){
            editor.commit();
        }
    }
    //値の保持
    protected void setSaveBoolean(String key, boolean val){
        setSaveBoolean(key, val, false);
    }
}
