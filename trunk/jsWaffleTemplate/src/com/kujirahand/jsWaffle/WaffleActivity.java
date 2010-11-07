package com.kujirahand.jsWaffle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WaffleActivity extends Activity {
	
	public WebView webview;
	public WaffleObj waffle_obj;
	public static String LOG_TAG = "DroidWaffle";
	protected LinearLayout root;
	protected Handler handler;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//
    	// 動的に作った WebView を View として登録する処理
    	super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        //
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        		ViewGroup.LayoutParams.FILL_PARENT, 0.0F);
        LinearLayout.LayoutParams webviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
        //
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setLayoutParams(containerParams);
        //
        webview = new WebView(this);
        webview.setLayoutParams(webviewParams);
        //
        handler = new Handler();
        // setting
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
        //
    }
       
    public void showPage(String uri) {
    	webview.loadUrl(uri);
        webview.requestFocus();
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
		  		view.loadUrl(url);
		  		return false;
		  	}
		  	return true;
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
			final LinearLayout layout = new LinearLayout(appContext);
			final EditText edtInput = new EditText(appContext);
			final TextView txtView = new TextView(appContext);
			
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(txtView);
			layout.addView(edtInput);
			
			txtView.setText(message);
			edtInput.setText(defaultValue);
			
			new AlertDialog.Builder(appContext)
                .setTitle("Prompt")
                .setView(layout)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = edtInput.getText().toString();
                                result.confirm(value);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                result.cancel();
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                result.cancel();
                            }
                        })
                .show();
            
            return true;
		}
		
	}
	
}




