package org.chromium.chrome.browser;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.material.snackbar.Snackbar;
import android.support.design.widget.Snackbar;
//import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.readlist.RListHelper;
import org.chromium.chrome.browser.readlist.ReadingListAdapter;
import org.chromium.chrome.browser.readlist.ReadingListModel;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;

import static android.view.View.GONE;

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

            //Picasso.get().load(lg_url).into(ReadLogo);

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

            
            findViewById(R.id.readshare_layout).setVisibility(GONE);

            Snackbar bar = Snackbar.make(findViewById(android.R.id.content), "Saved to Reading List", Snackbar.LENGTH_LONG).setAction("Action", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String activityToStart = "org.chromium.chrome.browser.document.ChromeLauncherActivity";
                    try {
                        Class<?> c = Class.forName(activityToStart);
                        Intent i = new Intent(getBaseContext(), c);
                        startActivity(i);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }).setDuration(2000);
            bar.show();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            };
            Handler handler = new android.os.Handler();
            handler.postDelayed(runnable, 2000);
            
            finish();
            Toast.makeText(this, "Saved to Reading List", Toast.LENGTH_LONG).show();
        }


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
