package com.example.uo.timecard.common;

import com.example.uo.timecard.other.JapaneseHolidayUtils;

import java.util.Calendar;

public class DateUtil {

    public static void main(String[] argv){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -5);
        System.out.println(cal);
    }


    //現在日（5時切替）が業務日付か調べる
    // 戻り値：true:業務日 false:休日
    public static boolean checkToday(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -5);

        return checkDate(cal);
    }

    //対象日が業務日付か調べる
    private static boolean checkDate(Calendar cal){
        //曜日チェック
        if(!checkWeek(cal)){
            return false;
        }
        //祝日チェック
        if(!checkHoliday(cal)){
            return false;
        }
        return true;
    }

    //曜日チェック
    private static boolean checkWeek(Calendar cal) {
        //土日の場合、休日
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if(week == Calendar.SATURDAY || week == Calendar.SUNDAY){
            return false;
        }
        return true;
    }

    //祝日チェック
    private static boolean checkHoliday(Calendar cal) {
        return  !JapaneseHolidayUtils.isHoliday(cal);
    }
}
