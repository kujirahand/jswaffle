package com.kujirahand.jsWaffle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.LinearLayout;

public class WaffleActivity extends Activity {
	
	public WebView webview;
	public WaffleObj waffle_obj;
	public static String LOG_TAG = "jsWaffle";
	protected LinearLayout root;
	protected Handler handler = new Handler();
	
	//----------------------------------------------------------------
	// Set Window Flags
	//----------------------------------------------------------------
	public void onSetWindowFlags(Window w) {
        w.requestFeature(Window.FEATURE_NO_TITLE);
        w.setFlags(
        		WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
       // * Full Screen
        // w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // * Keep Screen (Not Sleep)
        // w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	// Windowの初期化処理
        onSetWindowFlags(getWindow());
        
    	// 動的に作った WebView を View として登録する処理
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        		ViewGroup.LayoutParams.FILL_PARENT, 0.0F);
        LinearLayout.LayoutParams webviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
        //
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(containerParams);
        //
        webview = new WebView(this);
        webview.setLayoutParams(webviewParams);
        //
        // WebView Setting
        WebSettings setting = webview.getSettings();
        webview.setWebChromeClient(new jsWaffleChromeClient(this));
        webview.setWebViewClient(new jsWaffleWebViewClient(this));
        webview.setInitialScale(100);
        webview.setVerticalScrollBarEnabled(false);
        setting.setJavaScriptEnabled(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        // register special object
        waffle_obj = new WaffleObj(this);
        //
        Log.i(LOG_TAG, "Register Waffle to webview:" + waffle_obj);
        webview.addJavascriptInterface(waffle_obj, "_DroidWaffle");
        //
        root.addView(webview);
        setContentView(root);
    }
       
    public void showPage(String uri) {
    	webview.loadUrl(uri);
        webview.requestFocus();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	waffle_obj.onStat();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	waffle_obj.onStop();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	waffle_obj.onResume();
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	    	webview.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	//--- menu
	private MenuItemInfo menuInfo[] = new MenuItemInfo[6];
	public boolean setMenuItem(int itemNo, boolean visible, String title, String iconName) {
		if (0 <= itemNo && itemNo <= 5) {
			MenuItemInfo i = menuInfo[itemNo];
			if (i == null) {
				menuInfo[itemNo] = i = new MenuItemInfo();
			}
			i.visible = visible;
			i.title = title;
			i.iconName = iconName;
			return true;
		}
		return false;
	}
	private void setMenuItemSetting(Menu menu) {
		int resId;
		for (int i = 0; i < 6; i++) {
			MenuItemInfo info = menuInfo[i];
			if (info == null) {
				info = menuInfo[i] = new MenuItemInfo();
				info.visible = false;
			}
			MenuItem item = menu.getItem(i);
			item.setVisible(info.visible);
			resId = android.R.drawable.ic_btn_speak_now;
			if (info.visible) {
				item.setTitle(info.title);
				// icon
				resId = getResources().getIdentifier(
						info.iconName, "drawable",
						getPackageName()
						);
				if (resId == 0) {
					resId = getResources().getIdentifier(
							info.iconName, "drawable", "android");
				}
				if (resId > 0) {
					item.setIcon(resId);
				}
			}
		}
	}
	
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "menu0");
		menu.add(0, 1, 1, "menu1");
		menu.add(0, 2, 2, "menu2");
		menu.add(0, 3, 3, "menu3");
		menu.add(0, 4, 4, "menu4");
		menu.add(0, 5, 5, "menu5");
		setMenuItemSetting(menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setMenuItemSetting(menu);
	    return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		waffle_obj.onMenuItemSelected(item.getItemId());
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		waffle_obj.onActivityResult(requestCode, resultCode, intent);
	}
	
	
	/*
	// example fullscreen
	protected void updateFullscreenStatus(boolean bUseFullscreen)
	{   
	   if(bUseFullscreen)
	   {
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	    }
	    else
	    {
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    }
	    root.requestLayout();
	}
	*/
	
	//-------------------------------------------------------------------
	//WebViewClient
	//-------------------------------------------------------------------
	class jsWaffleWebViewClient extends WebViewClient {
		private Context appContext = null;
		public jsWaffleWebViewClient(Context con) {
			super();
			appContext = con;
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			boolean b = IntentHelper.run(appContext, url);
		  	if (!b) {
		  		view.loadUrl(url); // browse url in waffle browser
		  		return false;
		  	}
		  	return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			waffle_obj.callJsEvent("DroidWaffle.onPageFinished('" + url + "')");
		}
	}

	//-------------------------------------------------------------------
	// WebChromeClient
	//-------------------------------------------------------------------
	class jsWaffleChromeClient extends WebChromeClient {
		
		protected Context appContext = null;
		
		public jsWaffleChromeClient(Context con) {
			super();
			this.appContext = con;
		}
		
		// for Android 2.x
		public void addMessageToConsole(String message, int lineNumber, String sourceID) {
			Log.e(LOG_TAG, 
				sourceID + ": Line " + Integer.toString(lineNumber) + " : " +
				message);
		}
		
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
		{
			new AlertDialog.Builder(appContext)
				.setTitle("Information")
				.setMessage(message)
				.setPositiveButton(
					android.R.string.ok,
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) {
							result.confirm();
						}
					})
				.setCancelable(false) // true にすると[back]ボタンをで戻ったとき、再度このイベントが呼ばれない
				.create()
				.show();
			return true;
		}
		
		@Override
		public boolean onJsConfirm(WebView view, String url, String message, final JsResult result)
		{
		     new AlertDialog.Builder(appContext)
		        .setTitle("Confirm")
		        .setMessage(message)
		        .setPositiveButton(
		        		android.R.string.ok,
		        		new DialogInterface.OnClickListener()
		        {
		            public void onClick(DialogInterface dialog, int which)
		            {
		                result.confirm();
		            }
		        })
		        .setNegativeButton(android.R.string.cancel,
		                new DialogInterface.OnClickListener()
		        {
		            public void onClick(DialogInterface dialog, int which)
		            {
		                result.cancel();
		            }
		        })
		        .create()
		        .show();
		        return true;			
		}
		
		
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result)
		{
			// prompt() を拡張して様々な用途のダイアログを表示する
			DialogHelper.waffle_activity = WaffleObj.waffle_activity;
			DialogHelper.waffle_obj = waffle_obj;
			boolean r = true;
			switch (WaffleObj.dialogType) {
			case WaffleObj.DIALOG_TYPE_DEFAULT:
				r = DialogHelper.inputDialog("Prompt", message, defaultValue, result);
				break;
			case WaffleObj.DIALOG_TYPE_YESNO:
				r = DialogHelper.dialogYesNo(WaffleObj.dialogTitle, message, defaultValue, result);
				break;
			case WaffleObj.DIALOG_TYPE_SELECT_LIST:
				r = DialogHelper.selectList(WaffleObj.dialogTitle, message, defaultValue, result);
				break;
			case WaffleObj.DIALOG_TYPE_CHECKBOX_LIST:
				r = DialogHelper.checkboxList(WaffleObj.dialogTitle, message, defaultValue, result);
				break;
			case WaffleObj.DIALOG_TYPE_DATE:
				r = DialogHelper.datePickerDialog(WaffleObj.dialogTitle, message, defaultValue, result);
				break;
			case WaffleObj.DIALOG_TYPE_TIME:
				r = DialogHelper.timePickerDialog(WaffleObj.dialogTitle, message, defaultValue, result);
				break;
			case WaffleObj.DIALOG_TYPE_PROGRESS:
				r = DialogHelper.seekbarDialog(WaffleObj.dialogTitle, message, defaultValue, result);
				break;
			}
			return r;
		}
		
	}
}

// for Menu Item
class MenuItemInfo {
	public String title;
	public String iconName;
	public boolean visible;
}



