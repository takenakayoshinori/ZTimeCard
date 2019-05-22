package com.example.uo.timecard.common;

import java.io.Serializable;

//タイムカード時刻
public class TimeInfo implements Serializable {

	//出社時刻
	public String startTime = null;
	//退社時刻
	public String endTime = null;
	//出社ボタン表示
	public boolean dispStartBtn = false;
	//退社ボタン表示
	public boolean dispEndBtn = false;
	//エラーメッセージ
	public String errorMsg = null;
}
