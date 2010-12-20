package com.kujirahand.jsWaffle.plugin;

import java.util.Vector;

import android.content.Intent;

public class WafflePluginManager {
	
	public Vector<IWafflePlugin> items = new Vector<IWafflePlugin>();

	public void onPause() {
    	for (IWafflePlugin plugin : items) {
			plugin.onPause();
		}
	}
	public void onResume() {
    	for (IWafflePlugin plugin : items) {
			plugin.onResume();
		}
	}
	public void onDestroy() { // To remove listener from system
    	for (IWafflePlugin plugin : items) {
			plugin.onDestroy();
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	for (IWafflePlugin plugin : items) {
			plugin.onActivityResult(requestCode, resultCode, intent);
		}
	}
	public void onPageStarted(String url) {
		// 既に何かしらのリスナーが登録されているなら解除する
    	for (IWafflePlugin plugin : items) {
			plugin.onPageStarted(url);
		}
	}
	public void onPageFinished(String url) {
    	for (IWafflePlugin plugin : items) {
			plugin.onPageFinished(url);
		}
	}
}
