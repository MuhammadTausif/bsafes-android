package org.chromium.chrome.browser.readlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class RListHelper extends SQLiteOpenHelper {

    private static final String db_name = "rdb";
    private static final int version = 1;

    public RListHelper(Context context) {
        super(context, db_name, null, version);
    }

    public RListHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE READLIST (_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, description TEXT, logo_url TEXT, created LONG)";
        db.execSQL(sql);

        //insert sample
    }

    public void insertURLs(String url, String title, String logo_url, SQLiteDatabase db) {
        Date dDate = new Date();
        insertData(url, title, logo_url, dDate.getTime(), db);
    }

    private void insertData(String url, String descritpion, String logo_url, long created, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("description", descritpion);
        values.put("logo_url", logo_url);
        values.put("created", created);
        db.insert("READLIST", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
