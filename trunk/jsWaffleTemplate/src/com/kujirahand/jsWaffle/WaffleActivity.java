package com.kujirahand.jsWaffle;

import com.kujirahand.jsWaffle.model.IWafflePlugin;
import com.kujirahand.jsWaffle.model.WafflePluginManager;
import com.kujirahand.jsWaffle.plugins.AccelPlugin;
import com.kujirahand.jsWaffle.plugins.ContactPlugin;
import com.kujirahand.jsWaffle.plugins.DatabasePlugin;
import com.kujirahand.jsWaffle.plugins.DevInfoPlugin;
import com.kujirahand.jsWaffle.plugins.GPSPlugin;
import com.kujirahand.jsWaffle.plugins.ABasicPlugin;
import com.kujirahand.jsWaffle.plugins.StoragePlugin;
import com.kujirahand.jsWaffle.utils.DialogHelper;
import com.kujirahand.jsWaffle.utils.IntentHelper;

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
	
	/** jsWaffle Version Info */
	public static double WAFFLE_VERSON = 1.153;
	
	
	public static WaffleActivity mainInstance = null;
	public WebView webview;
	public static String LOG_TAG = "jsWaffle";
	protected LinearLayout root;
	protected Handler handler = new Handler();
	
	public WafflePluginManager pluginManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (mainInstance == null) mainInstance = this; // set main instance
    	
    	// Initialize Window
    	onSetWindowFlags(getWindow());
    	
    	// Create WebView
    	buildMainView();
        
    	// Set WebView Param
        setWebViewParams();
        
        // Set Plugins
        pluginManager = new WafflePluginManager(this);
        onAddPlugins();
        
        // Set WebView to Main View
        setContentView(root);
    }
   
    /**
     * set Window Flags
     * @param w
     */
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
	
    /**
     * 必要ならここでプラグインを追加する
     */
    protected void onAddPlugins() {
    	// main waffle object
    	addPlugin("_base", new ABasicPlugin()); // old '_DroidWaffle'
    	// plugins
    	addPlugin("_accel", new AccelPlugin());
    	addPlugin("_db", new DatabasePlugin());
    	addPlugin("_gps", new GPSPlugin());
    	addPlugin("_storage", new StoragePlugin());
    	addPlugin("_dev", new DevInfoPlugin());
    	addPlugin("_contact", new ContactPlugin());
    }
    
    protected IWafflePlugin addPlugin(String jsName, IWafflePlugin plugin) {
    	// add interface
        webview.addJavascriptInterface(plugin, jsName);
        pluginManager.items.add(plugin);
        // set parameters
        plugin.setContext(this);
        plugin.setWebView(webview);
        return plugin;
    }
    
    /**
     * Create main view
     */
    protected void buildMainView() {
    	// レイアウト用パラメータ
        LinearLayout.LayoutParams containerParams = 
        		new LinearLayout.LayoutParams(
        				ViewGroup.LayoutParams.FILL_PARENT, 
        				ViewGroup.LayoutParams.FILL_PARENT,
        				0.0F);
        LinearLayout.LayoutParams webviewParams = 
        		new LinearLayout.LayoutParams(
        				ViewGroup.LayoutParams.FILL_PARENT,
        				ViewGroup.LayoutParams.FILL_PARENT, 
        				1.0F);
        // ルートにレイアウトを追加
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(containerParams);
        //
        webview = new WebView(this);
        webview.setLayoutParams(webviewParams);
        root.addView(webview);
    }
    
    protected void setWebViewParams() {
    	webview.setWebChromeClient(new jsWaffleChromeClient(this));
        webview.setWebViewClient(new jsWaffleWebViewClient(this));
        webview.setInitialScale(100);
        webview.setVerticalScrollBarEnabled(false);
        
    	WebSettings setting = webview.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        setting.setBuiltInZoomControls(false);
    }
       
    public void showPage(String uri) {
    	webview.loadUrl(uri);
        webview.requestFocus();
    }

    /**
     * show log to DDMS LogCat
     * @param msg
     */
	public void log(String msg) {
		Log.d(WaffleActivity.LOG_TAG, msg);
	}
	public void log_error(String msg) {
		Log.e(WaffleActivity.LOG_TAG, msg);
	}
	public void log_warn(String msg) {
		Log.w(WaffleActivity.LOG_TAG, msg);
	}
	
	/**
	 * call JavaScript Event
	 * @param query
	 */
    public void callJsEvent(final String query) {
    	final String s = "javascript:" + query;
        handler.post(new Runnable() {
			@Override
			public void run() {
		        log(query);
				webview.loadUrl(s);
			}
		});
    }
    
    //-----------------------------------------------------------------
    // Activity Event
    //-----------------------------------------------------------------
    @Override
    protected void onStart() {
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected void onPause() {
    	super.onStop();
    	pluginManager.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	pluginManager.onResume();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	// call onunload event (for Android 2.x)
    	webview.loadUrl("about:blank");
    	pluginManager.onDestroy();
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
		int itemId = item.getItemId();
		callJsEvent(ABasicPlugin.menu_item_callback_funcname + "(" + itemId + ")");
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		pluginManager.onActivityResult(requestCode, resultCode, intent);
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
		  		log("loadUrl=" + url);
		  		view.loadUrl(url); // browse url in waffle browser
		  		return false;
		  	}
		  	return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			pluginManager.onPageStarted(url);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			pluginManager.onPageFinished(url);
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
						@Override
						public void onClick(DialogInterface dialog, int which) {
							result.confirm();
						}
					})
				.setCancelable(true)
		        .setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						result.cancel();
					}
				})
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
					@Override
		            public void onClick(DialogInterface dialog, int which)
		            {
		                result.confirm();
		            }
		        })
		        .setNegativeButton(android.R.string.cancel,
		                new DialogInterface.OnClickListener()
		        {
					@Override
		            public void onClick(DialogInterface dialog, int which)
		            {
		                result.cancel();
		            }
		        })
		        .setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						result.cancel();
					}
				})
		        .setCancelable(true)
		        .create()
		        .show();
		        return true;			
		}
		
		
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result)
		{
			// prompt() を拡張して様々な用途のダイアログを表示する
			DialogHelper.waffle_activity = mainInstance;
			boolean r = true;
			try {
				switch (ABasicPlugin.dialogType) {
				case ABasicPlugin.DIALOG_TYPE_DEFAULT:
					r = DialogHelper.inputDialog("Prompt", message, defaultValue, result);
					break;
				case ABasicPlugin.DIALOG_TYPE_YESNO:
					r = DialogHelper.dialogYesNo(ABasicPlugin.dialogTitle, message, defaultValue, result);
					break;
				case ABasicPlugin.DIALOG_TYPE_SELECT_LIST:
					r = DialogHelper.selectList(ABasicPlugin.dialogTitle, message, defaultValue, result);
					break;
				case ABasicPlugin.DIALOG_TYPE_CHECKBOX_LIST:
					r = DialogHelper.checkboxList(ABasicPlugin.dialogTitle, message, defaultValue, result);
					break;
				case ABasicPlugin.DIALOG_TYPE_DATE:
					r = DialogHelper.datePickerDialog(ABasicPlugin.dialogTitle, message, defaultValue, result);
					break;
				case ABasicPlugin.DIALOG_TYPE_TIME:
					r = DialogHelper.timePickerDialog(ABasicPlugin.dialogTitle, message, defaultValue, result);
					break;
				case ABasicPlugin.DIALOG_TYPE_PROGRESS:
					r = DialogHelper.seekbarDialog(ABasicPlugin.dialogTitle, message, defaultValue, result);
					break;
				}
			} catch (Exception e) {
				log("[DialogError]" + e.getMessage());
			}
			return r;
		}
		
		//Android 1.6 not supported
		@Override
		public boolean onJsBeforeUnload(android.webkit.WebView view, java.lang.String url, java.lang.String message, android.webkit.JsResult result)
		{
			log("[onJsBeforeUnload]");
			return false;
		}
	}
}

// for Menu Item
class MenuItemInfo {
	public String title;
	public String iconName;
	public boolean visible;
}



