package org.chromium.chrome.browser;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.readlist.RListHelper;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;

public class ReadShareDialog extends Activity {

    TextView BtnClose, ReadContent, ReadForward;
    ImageView Forward, ReadLogo;

    String sharedText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.readinglist_dialog);

        BtnClose = (TextView) findViewById(R.id.btn_readshare_close);
        BtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ReadContent = (TextView) findViewById(R.id.btn_readshare_content);
        ReadLogo = (ImageView) findViewById(R.id.iv_readshare_logo);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        final String[] value = new String[2];
        try{
            Thread nThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        /*InputStream is = (InputStream)new URL(sharedText).openStream();

                        Scanner scanner = new Scanner(is);
                        String responseBody = scanner.useDelimiter("\\A").next();

                        value[0] = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));*/

                        /*Document doc = Jsoup.connect(sharedText).get();

                        value[0] = doc.title();

                        Element element = doc.head().select("link[href~=.*\\.(ico|png)]").first();

                        if(isValidURL(element.attr("href")) ){
                            value[1] = element.attr("href");
                        } else {
                            if(element.attr("href").isEmpty() || !element.attr("href").equals("")){
                                value[1] = sharedText + element.attr("href");
                            }
                        }*/
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            nThread.start();
            nThread.join();
        }catch (Exception e){}

        displayText(sharedText, value[0], value[1]);

        Forward = (ImageView)findViewById(R.id.btn_readshare_forward);

        Forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendText(sharedText, value[0], value[1]);
            }
        });

        ReadForward = (TextView)findViewById(R.id.tv_readshare_forward);
        ReadForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendText(sharedText, value[0], value[1]);
            }
        });
    }

    void displayText(String url, String title, String logo_url) {

        final String lg_url = logo_url;

        if(url != null) {
            ReadContent.setText(title);
            ReadContent.append("\n");
            ReadContent.append(url);

            new Thread(new Runnable() {
                String abc = "";

                @Override
                public void run() {
                    try {
                        InputStream is = (InputStream)new URL(lg_url).getContent();
                        ReadLogo.setImageBitmap(BitmapFactory.decodeStream(is));
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    void handleSendText(String url, String title, String logo_url) {

        if(url != null) {
            RListHelper rListHelper = new RListHelper(getBaseContext());
            SQLiteDatabase db = rListHelper.getReadableDatabase();
            rListHelper.insertURLs(url, title, logo_url, db);

            Toast.makeText(this, "Saved to Reading List", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    public boolean isValidURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return true;
        }
        catch (MalformedURLException e) {
            return false;
        }
    }


}
