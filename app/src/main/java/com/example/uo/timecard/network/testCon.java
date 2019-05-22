package com.example.uo.timecard.network;

public class testCon {
    public static void main(String[] args){
        String url = "https://zexis.jp/ad/ptw/ag.cgi?";
        String data = "";
        HtmlConnect hcon = new HtmlConnect();
        hcon.getPageData(url, data);
    }
}
