package org.chromium.chrome.browser.readlist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import org.chromium.base.VisibleForTesting;
import org.chromium.chrome.browser.IntentHandler;
import org.chromium.chrome.browser.SnackbarActivity;
import org.chromium.chrome.browser.util.IntentUtils;
import org.chromium.chrome.R;

public class RListActivity extends SnackbarActivity {

    ListView lv_rlist;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readinglist_main);

        RListHelper rListHelper = new RListHelper(this);
        SQLiteDatabase db = rListHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT url, created FROM READLIST", new String[]{});

        if(cursor != null) {
            cursor.moveToFirst();
        }

        ArrayList<String> ar = new ArrayList<String>();

        do {
            String url = cursor.getString(0);
            int created = cursor.getInt(1);

            ar.add(url);

            Toast.makeText(this, url + "" + created, Toast.LENGTH_SHORT).show();
        } while (cursor.moveToNext());

        lv_rlist = (ListView) findViewById(R.id.lv_rlist);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, ar);
        lv_rlist.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}