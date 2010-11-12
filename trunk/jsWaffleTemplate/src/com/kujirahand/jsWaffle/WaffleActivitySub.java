package com.kujirahand.jsWaffle;

import android.content.Intent;
import android.os.Bundle;

public class WaffleActivitySub extends WaffleActivity {
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
}
