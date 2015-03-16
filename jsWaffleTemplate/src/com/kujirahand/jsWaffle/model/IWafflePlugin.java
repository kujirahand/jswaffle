package com.kujirahand.jsWaffle.model;

import com.kujirahand.jsWaffle.WaffleActivity;

import android.content.Intent;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;


/**
 * jsWaffle Plugin Interfalce
 *
 * @author kujira
 */

public interface IWafflePlugin {
    /* initialize method */
    @JavascriptInterface
    void setWebView(WebView web);

    @JavascriptInterface
    void setContext(WaffleActivity app);

    /* activity events */
    @JavascriptInterface
    void onPause();

    @JavascriptInterface
    void onResume();

    // void onUnload(); // Android1.6 not supported
    @JavascriptInterface
    void onDestroy(); // To remove listener from system

    @JavascriptInterface
    void onActivityResult(int requestCode, int resultCode, Intent intent);

    /* webview event */
    @JavascriptInterface
    void onPageStarted(String url);

    @JavascriptInterface
    void onPageFinished(String url);
}
