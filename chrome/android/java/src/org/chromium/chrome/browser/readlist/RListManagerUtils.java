package org.chromium.chrome.browser.readlist;

import android.content.Context;
import android.content.Intent;

import org.chromium.base.ContextUtils;
import org.chromium.chrome.browser.ChromeActivity;
import org.chromium.chrome.browser.IntentHandler;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.chrome.browser.util.UrlConstants;
import org.chromium.content_public.browser.LoadUrlParams;

public class RListManagerUtils {
    /**
     * Opens the browsing history manager.
     *
     * @param activity The {@link ChromeActivity} that owns the {@link HistoryManager}.
     * @param tab The {@link Tab} to used to display the native page version of the
     *            {@link HistoryManager}.
     */
    public static void showRListManager(ChromeActivity activity, Tab tab) {
        Context appContext = ContextUtils.getApplicationContext();
        // if (activity.isTablet()) {
        //     // History shows up as a tab on tablets.
        //     LoadUrlParams params = new LoadUrlParams(UrlConstants.NATIVE_HISTORY_URL);
        //     tab.loadUrl(params);
        // } else {
            
        // }

        Intent intent = new Intent();
        intent.setClass(appContext, RListActivity.class);
        activity.startActivity(intent);
    }
}
