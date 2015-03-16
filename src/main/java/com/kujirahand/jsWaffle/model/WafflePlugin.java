package com.kujirahand.jsWaffle.model;

import com.kujirahand.jsWaffle.WaffleActivity;

import android.content.Intent;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;

public class WafflePlugin implements IWafflePlugin {

    protected WebView webview;
    protected WaffleActivity waffle_activity;

    @Override
    @JavascriptInterface
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    @Override
    @JavascriptInterface
    public void onDestroy() {
    }

    @Override
    @JavascriptInterface
    public void onPause() {
    }

    @Override
    @JavascriptInterface
    public void onResume() {
    }

    @Override
    @JavascriptInterface
    public void onPageStarted(String url) {
    }

    @Override
    @JavascriptInterface
    public void onPageFinished(String url) {
    }

    @Override
    @JavascriptInterface
    public void setContext(WaffleActivity app) {
        this.waffle_activity = app;
    }

    @Override
    @JavascriptInterface
    public void setWebView(WebView web) {
        this.webview = web;
    }
}
