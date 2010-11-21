package com.kujirahand.template;

import com.kujirahand.jsWaffle.WaffleActivity;
import android.os.Bundle;

public class ShowHTML extends WaffleActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.showPage("file:///android_asset/www/index.html");
    	
    	// example
    	//this.showPage("http://192.168.100.2/test/a.html?r=" + Math.random()); // localhost test
    	//this.showPage("file:///android_asset/www/test/database.html"); // assets
    }
}