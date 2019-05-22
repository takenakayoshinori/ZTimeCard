package com.example.uo.timecard.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//画面への接続
public class HtmlConnect {
	CookieManager man = null;
	public String urlPath = null;
	public String sendData = null;
	public List<String> pageDateList = null;

	HtmlConnect(){
		this.man = new CookieManager();
	}

	//HTMLページからデータ取得
	public List<String> getPageData(String urlPath, String sendData){
		HttpURLConnection con = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		List<String> pageDateList = new ArrayList<String>();
		try {
			//接続
			con = createConnention(urlPath, sendData);
			//responseの取得
			isr = new InputStreamReader(con.getInputStream(), "utf-8");
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				pageDateList.add(line);
			}
			return pageDateList;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				br.close();
				isr.close();
				con.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//Connection作成
	public HttpURLConnection createConnention(String urlPath, String sendData) throws IOException {
		URL url = new URL(urlPath);
		HttpURLConnection con = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;

		CookieHandler.setDefault(this.man);

		con = (HttpURLConnection) url.openConnection();

		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestMethod("POST");
		con.connect();

		os = con.getOutputStream();
		osw = new OutputStreamWriter(os, "utf-8");

		osw.write(sendData);
		osw.close();

		return con;
	}
}
