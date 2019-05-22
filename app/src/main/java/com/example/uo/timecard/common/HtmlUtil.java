package com.example.uo.timecard.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Htmlデータの操作
public class HtmlUtil {

	//htmlからformタブの中身のみ取得
	public static List<String> getFormData(List<String> dataList){

		List<String> formDataList = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for(String line:dataList){
			if(line.indexOf("<form") >= 0){
				sb = new StringBuffer();
			}
			sb.append(line).append("\n");

			if(line.indexOf("</form") >= 0){
				formDataList.add(sb.toString());
				sb = new StringBuffer();
			}
		}
		return formDataList;
	}

	//HTMLの内容からパラメータを抜き出す（Map）
	public static Map<String, String> createParamMap(String[] formDataList){
		HashMap<String, String> paramMap = new HashMap<String, String>();
		for(String line:formDataList){
			if(line.indexOf("type=\"hidden\"") >= 0 || line.indexOf("type=hidden") >= 0 ){
				String name = getParam(line,"name=\"","\"");
				String value = getParam(line,"value=\"","\"");
				if(name == null || value == null){
					continue;
				}
				paramMap.put(name, value);
			}
		}
		return paramMap;
	}

	//HTMLの内容からパラメータを抜き出す（引数）
	public static String createParam(String[] formDataList){
		StringBuffer sb = new StringBuffer();
		try{
			Map<String, String> paramMap = createParamMap(formDataList);
			for(Map.Entry<String, String> entry : paramMap.entrySet()) {
				sb.append("&").append(entry.getKey()).append("=")
				.append(URLEncoder.encode(entry.getValue(), "utf-8"));
			}
		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return sb.toString();
	}

	//パラメータの作成
	public static String toParam(String name, String value){
		try{
			return name + "=" + URLEncoder.encode(value, "utf-8");
		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
			return null;
		}
	}
	//値の抜き出し
	public static String getParam(String data, String beforeWord, String endWord){
		int start = data.indexOf(beforeWord);
		if(start < 0){
			return null;
		}
		int end = data.indexOf(endWord, start + beforeWord.length());
		if(end < 0){
			return null;
		}
		return data.substring(start + beforeWord.length(), end);
	}
}
