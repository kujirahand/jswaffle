package com.kujirahand.jsWaffle;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class WaffleActivityFullScreen extends WaffleActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        // Check url (Intent Param)
        Intent i = getIntent();
        String url = i.getStringExtra("url");
        if (url != null) {
        	this.showPage(url);
        }
        else {
        	this.showPage("file:///android_asset/www/index.html");
        }
    }
    @Override
	public void onSetWindowFlags(Window w) {
        w.requestFeature(Window.FEATURE_NO_TITLE);
        // * Full Screen
        w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

}
