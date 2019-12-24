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

    ArrayList<ReadingListModel> dataModel;
    ListView ReadingListView;
    private static ReadingListAdapter adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readinglist_main);

        ReadingListView = (ListView) findViewById(R.id.lv_rlist);

        RListHelper rListHelper = new RListHelper(this);
        SQLiteDatabase db = rListHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT url, description, logo_url, created FROM READLIST", new String[]{});

        if(cursor != null) {
            cursor.moveToFirst();
        }

        dataModel = new ArrayList<>();

        do {
            String url = cursor.getString(0);
            String title = cursor.getString(1);
            String logo_url = cursor.getString(2);

            dataModel.add(new ReadingListModel(url, title, logo_url));


        } while (cursor.moveToNext());

        adapter = new ReadingListAdapter(dataModel, getApplicationContext());
        ReadingListView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
