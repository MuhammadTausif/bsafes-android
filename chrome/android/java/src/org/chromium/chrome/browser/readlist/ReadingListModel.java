package com.pro.myapplication;

public class ReadingListModel {
    String title, url, logo_url;

    public ReadingListModel(String url, String title, String logo_url) {
        this.url = url;
        this.title = title;
        this.logo_url = logo_url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl(){
        return url;
    }

    public String getLogoURL(){
        return logo_url;
    }
}
