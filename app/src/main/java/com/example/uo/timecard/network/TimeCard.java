package com.example.uo.timecard.network;

import com.example.uo.timecard.common.HtmlUtil;
import com.example.uo.timecard.common.TimeInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

//タイムカード
public class TimeCard {

	//サイボーズURL
	private static final String CIBOZE_URL = "https://zexis.jp/ad/ptw/ag.cgi?";
	//タイムカードURL
	private static final String TIMECARD_URL = "https://zexis.jp/ad/ptw/ag.cgi?page=TimeCardIndex";
	//出社情報
	private TimeInfo timeInfo = new TimeInfo();

	//ユーザー
	private String user = null;
	//パスワード
	private String passwd = null;

	//タイムカードのHTML内容
	public String timeCardData = null;

	//初期表示処理
	public static TimeCard getInstance(String user, String passwd){

		TimeCard timeCard = new TimeCard();
		timeCard.user = user;
		timeCard.passwd = passwd;

		//メニュー画面接続（タイムカード情報の抜き出し）
		if(!timeCard.getTimeCardInfo()){
			return timeCard;
		}

		//出社ボタン存在チェック
		if(timeCard.timeCardData
				.indexOf("<input class=\"vr_stdButton\" type=\"submit\" name=\"PIn\" value=\"出社\">") >= 0){
			timeCard.timeInfo.dispStartBtn = true;
		} else {
			timeCard.setStartTime();
		}
		//退社ボタン存在チェック
		if(timeCard.timeCardData
				.indexOf("<input class=\"vr_stdButton\" type=\"submit\" name=\"POut\" value=\"退社\">") >= 0){
			timeCard.timeInfo.dispEndBtn = true;
		} else {
			timeCard.setEndTime();
		}
		return timeCard;
	}

	//newを禁止
	private void checkTimeInfo(){
	}

	//出社処理
	public void syusya(){
		timeCardAct("PIn", "出社");
	}

	//退社処理
	public void taisya(){
		timeCardAct("POut", "退社");
	}

	//メニュー画面接続（タイムカード情報の抜き出し）
	public boolean getTimeCardInfo(){

		String sendData = getLoginParam();
		System.out.println(sendData);

		HtmlConnect connect = new HtmlConnect();
		List<String> dataList = connect.getPageData(CIBOZE_URL, sendData);
		List<String> formDataList = HtmlUtil.getFormData(dataList);
		//タイムカード情報の抜き出し
		for(String line:formDataList){
			if(line.indexOf("value=\"AjaxXHRPortletTimeCard\"") >= 0){
				timeCardData = line;
				return true;
			}
		}
		//エラー内容の調査
		for(String line:dataList){
			String val = HtmlUtil.getParam(line,"<div id=\"ErrorMessage\">","</div>");
			if(val != null){
                timeInfo.errorMsg = val;
				return false;
			}
		}
		return false;
	}

	//出社、退社処理
	public void timeCardAct(String actName, String actValue){
		String sendData = getLoginParam();
		HtmlConnect connect = new HtmlConnect();
		List<String> dataList = connect.getPageData(TIMECARD_URL, sendData);
		List<String> formDataList = HtmlUtil.getFormData(dataList);

		//タイムカード情報の抜き出し
		for(String line:formDataList){
			if(line.indexOf("name=\"page\" value=\"TimeCardIndex\"") >= 0){
				timeCardData = line;
			}
		}
		//パラメータの取得
		String sendData2 = HtmlUtil.toParam(actName, actValue) + HtmlUtil.createParam(timeCardData.split("\n"));

		//パラメータにボタン情報を追加する
		List<String> dataList2 = connect.getPageData(CIBOZE_URL, sendData2);
		//チェック処理はそのうち・・・
//		for(String line:dataList2){
//			System.out.println(line);
//		}
	}

	//ログイン情報取得
	public String getLoginParam(){
		try {
			return "_Account=" + URLEncoder.encode(user, "utf-8") +
					"&Password=" + URLEncoder.encode(passwd, "utf-8") +
					"&_System=" + URLEncoder.encode("login", "utf-8") +
					"&_Login=" + URLEncoder.encode("1", "utf-8") +
					"&LoginMethod=" + URLEncoder.encode("2", "utf-8") +
					"&csrf_ticket=" + URLEncoder.encode("", "utf-8");

		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
            timeInfo.errorMsg = "エラー発生。";
			return null;
		}
	}

	//出社時刻のセット
	public void setStartTime(){
		for(String line:timeCardData.split("\n")){
			String val = HtmlUtil.getParam(line,"出社<br>","</td>");
			if(val != null){
				timeInfo.startTime = val;
				return;
			}
		}
	}

	//退社時刻のセット
	public void setEndTime(){
		for(String line:timeCardData.split("\n")){
			String val = HtmlUtil.getParam(line,"退社<br>","</td>");
			if(val != null){
				timeInfo.endTime = val;
				return;
			}
		}
	}

	//時刻情報の取得
	public TimeInfo getTimeInfo() {
		return timeInfo;
	}
}
