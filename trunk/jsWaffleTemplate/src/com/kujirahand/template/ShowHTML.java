package com.kujirahand.template;

import com.kujirahand.jsWaffle.WaffleActivity;

import android.os.Bundle;

public class ShowHTML extends WaffleActivity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	// show index.html
    	this.showPage("file:///android_asset/www/index.html");
    }
    
    @Override
    protected void onAddPlugins() {
    	super.onAddPlugins();
    }
}