package com.kujirahand.jwtemplate;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kujirahand.jsWaffle.WaffleActivity;
import com.kujirahand.jsWaffle.model.WaffleFlags;

public class jwTemplate extends WaffleActivity {
    
    /** Set jsWaffle Setting flags */
    @Override
    protected void onSetWaffleFlags(WaffleFlags flags) {
    	super.onSetWaffleFlags(flags);
    	// set flags
    	flags.mainHtmlUrl = "file:///android_asset/www/index.html";
    	flags.keepScreenNotSleep = false;
    	flags.useFullScreen = false;
    	flags.useVerticalScrollBar = false;
    	flags.setWidth(320);
    }

    @Override
    public void showPage(String uri) {
    	super.showPage(uri);
    	
    }
    
    /** Please add the custom plug-in if it is necessary. */
    @Override
    protected void onAddPlugins() {
    	super.onAddPlugins();
    }
}