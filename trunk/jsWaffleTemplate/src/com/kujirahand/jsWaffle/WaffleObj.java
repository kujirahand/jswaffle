package com.kujirahand.jsWaffle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.MenuItem.OnMenuItemClickListener;
import android.webkit.WebView;
import android.widget.Toast;

/*
 * Javaクラスのメソッドのみが登録される
 */
public class WaffleObj
{
	public static double WAFFLE_VERSON = 1.05;
	//
	public static int ACTIVITY_REQUEST_CODE_BARCODE = 0xFF0001;
	//
	public static WaffleActivity waffle_activity = null;
	public static boolean flag_sensor = false;
	public AccelListener accel_listener = null;
	public static float accelX = 0;
	public static float accelY = 0;
	public static float accelZ = 0;
	public static String event_onStart = null;
	public static String event_onStop = null;
	public static String event_onResume = null;
	//
	private WebView webview;
	
	// callback string
	private String intent_startActivity_callback = null;
	private String intent_startActivity_callback_barcode = null;
	
	// constructor
	public WaffleObj(WaffleActivity activity) {
		waffle_activity = activity;
		webview = waffle_activity.webview;
	}
	
	//---------------------------------------------------------------
	// Interface
	//---------------------------------------------------------------
	/**
	 * Log
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
	/***
	 * Get Waffle Version Info
	 * @return version string
	 */
	public double getWaffleVersion() {
		return WAFFLE_VERSON;
	}
	
	/**
	 * Set Sensor event callback
	 * @param funcname
	 */
	public void setAccelCallback(String funcname) {
		if (accel_listener == null) {
			accel_listener = new AccelListener(waffle_activity, this);
		}
		accel_listener.sensour_callback_funcname = funcname;
		if (funcname == "") {
			stopSensor();
			flag_sensor = false;
			return;
		}
        startSensor();
		flag_sensor = true;
	}
	
	/**
	 * Set Shake Event callback
	 */
	public void setShakeCallback(String funcname, double freq) {
		if (accel_listener == null) {
			accel_listener = new AccelListener(waffle_activity, this);
		}
		if (funcname == "") {
			stopSensor();
			flag_sensor = false;
			return;
		}
		accel_listener.shake_freq = freq;
		accel_listener.shake_callback_funcname = funcname;
		flag_sensor = true;
		startSensor();
	}
	
	private ArrayList<GeoListener> geolocation_listeners = new ArrayList<GeoListener>();
	
	/**
	 * geolocation_getCurrentPosition
	 */
	public int geolocation_getCurrentPosition(String callback_ok, String callback_ng, boolean accuracy_fine) {
		GeoListener geo_listener = new GeoListener(waffle_activity, this, 1);
		geo_listener.callback_success = callback_ok;
		geo_listener.callback_failed = callback_ng;
		geo_listener.flagLive = true;
		geo_listener.start(accuracy_fine);
		geolocation_listeners.add(geo_listener);
		return geolocation_listeners.size();
	}
	
	/**
	 * geolocation_watchPosition
	 * @param callback_ok
	 * @param callback_ng
	 * @return watchId
	 */
	public int geolocation_watchPosition(String callback_ok, String callback_ng, boolean accuracy_fine) {
		GeoListener geo_listener = new GeoListener(waffle_activity, this, 0);
		geo_listener.callback_success = callback_ok;
		geo_listener.callback_failed = callback_ng;
		geo_listener.flagLive = true;
		geo_listener.start(accuracy_fine);
		geolocation_listeners.add(geo_listener);
		return geolocation_listeners.size();
	}
	
	/**
	 * geolocation_clearWatch
	 * @param watchId
	 */
	public void geolocation_clearWatch(int watchId) {
		try {
			int index = watchId - 1;
			if (geolocation_listeners.size() <= index) return;
			GeoListener i = geolocation_listeners.get(index);
			if (i == null) return;
			i.flagLive = false;
			i.stop();
			geolocation_listeners.set(index, null);
		} catch (Exception e) {
			log_error("geolocation_clearWatch:" + e.getMessage());
		}
	}
	
	
	/**
	 * beep
	 */
	public void beep() {
		if (beep_tone == null) {
			Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			beep_tone = RingtoneManager.getRingtone(waffle_activity, ringtone);
			if (beep_tone == null) {
				ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				beep_tone = RingtoneManager.getRingtone(waffle_activity, ringtone);
			}
		}
		if (beep_tone.isPlaying()) return;
		beep_tone.play();

	}
	Ringtone beep_tone = null;
	/**
	 * ring
	 */
	public void ring() {
		if (ring_tone == null) {
			Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			ring_tone = RingtoneManager.getRingtone(waffle_activity, ringtone);
		}
		if (ring_tone.isPlaying()) return;
		ring_tone.play();
	}
	Ringtone ring_tone = null;
	
	/**
	 * vibrate
	 * @param msec
	 */
	public void vibrate(long msec) {
		long pattern = msec;
		if (pattern == 0) pattern = 500;
        Vibrator vibrator = (Vibrator) waffle_activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern);
	}
	/**
	 * makeToast
	 * @param msg
	 */
	public void makeToast(String msg) {
		Toast.makeText(waffle_activity, msg, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * saveText
	 * @param filename
	 * @param text
	 * @retrun result
	 */
	public boolean saveText(String filename, String text) {
		try {
			FileOutputStream output = null;
			Uri uri = Uri.parse(filename);
			String scheme = uri.getScheme();
			if (scheme == null) {
				output = waffle_activity.openFileOutput(filename, Context.MODE_PRIVATE);
			}
			else if (scheme.equals("file")) {
				File f = new File(uri.getPath());
				output = new FileOutputStream(f);
			}
			else {
				return false;
			}
			output.write(text.getBytes());
			output.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * loadText
	 * @param filename
	 * @return text
	 */
	public String loadText(String filename) {
		try {
			FileInputStream input = null;
			Uri uri = Uri.parse(filename);
			String scheme = uri.getScheme();
			if (scheme == null) {
				input = waffle_activity.openFileInput(filename);
			}
			else if (scheme.equals("file")) {
				File f = new File(uri.getPath());
				input = new FileInputStream(f);
			}
			else {
				return null;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuffer buf = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
			}
			reader.close();
			return buf.toString();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * Check file exists
	 * @param filename
	 * @return true or false
	 */
	public Boolean fileExists(String filename) {
		File file = null;
		Uri uri = Uri.parse(filename);
		if (uri.getScheme() == null) {
			file = waffle_activity.getFileStreamPath(filename);
		}
		else { // file
			file = new File(uri.getPath());
		}
		return file.exists();
	}
	/**
	 * get file list
	 * @param path
	 * @return filenames (splitter ";")
	 */
	public String fileList(String path) {
		//TODO: fileList with file scheme
		File dir = waffle_activity.getFilesDir();
		File[] files = dir.listFiles();
		String r = "";
		for (int i = 0; i < files.length; i++) {
			String f = files[i].getName();
			Log.d(WaffleActivity.LOG_TAG, f);
			r += f + ";";
		}
		if (r != "") {
			r = r.substring(0, r.length() - 1);
		}
		return r;
	}
	//---------------------------------------------------------------
	// preference method
	//---------------------------------------------------------------
	/**
	 * get public preference
	 * @return
	 */
	private SharedPreferences getPublicPreference() {
		String pref_name = waffle_activity.getPackageName() + ".public";
		SharedPreferences pref = waffle_activity.getSharedPreferences(
				pref_name, Activity.MODE_WORLD_READABLE | Activity.MODE_WORLD_WRITEABLE);
		return pref;
	}
	/**
	 * Preference put
	 * @param key
	 * @param value
	 */
	public void preferencePut(String key, String value) {
		Editor e = getPublicPreference().edit();
		e.putString(key, value);
		e.commit();
	}
	/**
	 * Preferece get
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String preferenceGet(String key, String defaultValue) {
		return getPublicPreference().getString(key, defaultValue);
	}
	/**
	 * Preference exists key
	 * @param key
	 * @return
	 */
	public boolean preferenceExists(String key) {
		return getPublicPreference().contains(key);
	}
	/**
	 * Preference clear
	 */
	public void preferenceClear() {
		Editor e = getPublicPreference().edit();
		e.clear();
		e.commit();
	}
	/**
	 * Preference remove
	 */
	public void preferenceRemove(String key) {
		Editor e = getPublicPreference().edit();
		e.remove(key);
		e.commit();
	}
	//---------------------------------------------------------------
	/**
	 * get private preference
	 * @return
	 */
	private SharedPreferences getPrivatePreference() {
		String pref_name = waffle_activity.getPackageName() + ".private";
		SharedPreferences pref = waffle_activity.getSharedPreferences(
				pref_name, Activity.MODE_PRIVATE);
		return pref;
	}
	/**
	 * local storage : Preference put
	 * @param key
	 * @param value
	 */
	public void localStorage_put(String key, String value) {
		Editor e = getPrivatePreference().edit();
		e.putString(key, value);
		e.commit();
	}
	/**
	 * local storage : Preferece get
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String localStorage_get(String key, String defaultValue) {
		return getPrivatePreference().getString(key, defaultValue);
	}
	/**
	 * local storage : Preference exists key
	 * @param key
	 * @return
	 */
	public boolean localStorage_exists(String key) {
		return getPrivatePreference().contains(key);
	}
	/**
	 * local storage : Preference clear
	 */
	public void localStorage_clear() {
		Editor e = getPrivatePreference().edit();
		e.clear();
		e.commit();
	}
	public void localStorage_remove(String key) {
		Editor e = getPublicPreference().edit();
		e.remove(key);
		e.commit();
	}
	
	/**
	 * open database
	 * @param dbname
	 * @return DBHelper object
	 */
	public DBHelper openDatabase(String dbname) {
		DBHelper db = new DBHelper(waffle_activity, this);
		boolean b = db.openDatabase(dbname);
		if (!b) return null;
		return db;
	}
	public void executeSql(DBHelper db, String sql, String fn_ok, String fn_ng) {
		if (!(db instanceof DBHelper)) {
			log_error("executeSql : db is not DBHelper instance!!");
			return;
		}
		db.callback_result = fn_ok;
		db.callback_error = fn_ng;
		db.executeSql(sql, null, null);
	}
	/**
	 * play media file
	 * @param filename
	 * @return MediaPlayer
	 */
	public MediaPlayer createPlayer(String soundfile) {
		try {
			MediaPlayer mp = new MediaPlayer();
			// parse uri
			Uri uri = Uri.parse(soundfile);
			String scheme = uri.getScheme();
			log("scheme:"+ uri.getScheme());
			log("host:"+ uri.getHost());
			log("path:"+uri.getPath());
			
			// check path
			if (scheme == null) {
				// relative path
				log("[audio] assets:" + soundfile);
				AssetFileDescriptor fis = null;
				try {
					fis = waffle_activity.getAssets().openFd(soundfile);
				} catch (Exception e) {
					try {
						fis = waffle_activity.getAssets().openFd("www/" + soundfile);
					} catch (Exception e2) {
						fis = null;
					}
				}
				if (fis != null) {
					mp.setDataSource(fis.getFileDescriptor());
					mp.prepare();
				}
			}
			else if (scheme == "file") {
				log("[audio] file:" + soundfile);
				mp.setDataSource(uri.getPath());
				mp.prepare();
			}
			else {
				log_error("[audio]Path Error:" + soundfile);
				return null;
			}
			return mp;
		} catch (IOException e) {
			log_error("[audio]" + e.getMessage() + "/" + soundfile);
			return null;
		}
	}
	public void playPlayer(MediaPlayer mp) {
		mp.start();
	}
	public void stopPlayer(MediaPlayer mp) {
		mp.stop();
	}
	
	/**
	 * Start Intent
	 * @param url
	 */
	public boolean startIntent(String url) {
		return _startIntent(url, false);
	}
	/**
	 * Start Intent with FullScreen
	 * @param url
	 */
	public boolean startIntentFullScreen(String url) {
		return _startIntent(url, true);
	}
	
	private boolean _startIntent(String url, boolean bFull) {
		// android_asset?
		if (url.startsWith("file:///android_asset/")) {
			try {
				Intent i;
				if (bFull) {
					i = new Intent(waffle_activity, WaffleActivityFullScreen.class);
				} else {
					i = new Intent(waffle_activity, WaffleActivitySub.class);
				}
				i.putExtra("url", url);
				i.setAction(Intent.ACTION_VIEW);
				waffle_activity.startActivityForResult(i, 1);
				return true;
			} catch (Exception e) {
				log_error("assets:" + e.getMessage());
				return false;
			}
		}
		// other
		else {
			return IntentHelper.run(waffle_activity, url);
		}
	}
	
	/**
	 * new Intent
	 */
	public Intent newIntent(String action, String uri) {
		Intent intent = new Intent(action, Uri.parse(uri));
		return intent;
	}
	public void intent_putExtra(Intent intent, String name, String value) {
		intent.putExtra(name, value);
	}
	public String intent_getExtra(Intent intent, String name) {
		return intent.getStringExtra(name);
	}
	/**
	 * Start Intent
	 * @param intent
	 */
	public void intent_startActivity(Intent intent) {
		try {
			waffle_activity.startActivity(intent);
		} catch (Exception e) {
			log_error("activityError:" + e.getMessage());
		}
	}
	
	/**
	 * Start Intent (and Request Result)
	 * @param intent
	 * @param requestCode
	 */
	public void intent_startActivityForResult(Intent intent, int requestCode, String callbackName) {
		try {
			this.intent_startActivity_callback = callbackName;
			waffle_activity.startActivityForResult(intent, requestCode);
		} catch (Exception e) {
			log_error("activityError:" + e.getMessage());
		}
	}
	
	/**
	 * Get Barcode
	 * @param callbackName
	 * @param mode (null|QR_CODE_MODE|ONE_D_MODE|DATA_MATRIX_MODE)
	 * @return tried
	 */
	public boolean scanBarcode(String callbackName, String mode) {
		if (!intent_exists("com.google.zxing.client.android.SCAN")) {
			return false;
		}
		intent_startActivity_callback_barcode = callbackName;
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			if (mode == "QR_CODE_MODE" || mode == "ONE_D_MODE" || mode == "DATA_MATRIX_MODE") {
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			}
			waffle_activity.startActivityForResult(intent, ACTIVITY_REQUEST_CODE_BARCODE);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Intent Exists?
	 * @param intentName (ex: com.kujirahand.jsWaffle.xxx)
	 */
	public boolean intent_exists(String intentName) {
		final Intent intent = new Intent(intentName);
		final PackageManager packMan = waffle_activity.getPackageManager();
		List<ResolveInfo> list = packMan.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return (list.size() > 0);
	}
	
	/**
	 * finish activity
	 */
	public void finish() {
		waffle_activity.finish();
	}
	
	/**
	 * Add menu
	 */
	public void setMenuItem(int itemNo, boolean visible, String title, String iconName) {
		waffle_activity.setMenuItem(itemNo, visible, title, iconName);
	}
	public static String menu_item_callback_funcname = null;
	public void setMenuItemCallback(String callback_fn) {
		menu_item_callback_funcname = callback_fn;
	}
	
	/**
	 * Dialog
	 */
	public void dialogYesNo(String caption, String msg, final String callback_fn, final int tag) {
		final WaffleObj wobj = this;
		new AlertDialog.Builder(waffle_activity)
		.setIcon(android.R.drawable.ic_menu_help)
		.setTitle(caption)
		.setMessage(msg)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	wobj.callJsEvent(String.format("%s(%s,%d)", callback_fn, "true", tag));
		    }
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	wobj.callJsEvent(String.format("%s(%s,%d)", callback_fn, "false", tag));
		    }
		})
		.show();
	}
	public void selectList(String caption, String items, final String callback_fn, final int tag) {
		// items split
		final String[] str_items = items.split(";;;");
		//
		final WaffleObj wobj = this;
		new AlertDialog.Builder(waffle_activity)
		.setTitle(caption)
		.setItems(str_items, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				String ans = str_items[which];
				ans = ans.replaceAll("\"", "''");
		    	wobj.callJsEvent(String.format("%s(\"%s\",%d)", 
		    			callback_fn, 
		    			ans, 
		    			tag));
			}
		})
		.show();
	}
	// event callback
	public void registerActivityOnStart(String callback_fn) {
		event_onStart = callback_fn;
	}
	public void registerActivityOnStop(String callback_fn) {
		event_onStop = callback_fn;
	}
	public void registerActivityOnResume(String callback_fn) {
		event_onResume = callback_fn;
	}
	
	//---------------------------------------------------------------
	// Event Wrapper
	//---------------------------------------------------------------
	public void onStat() {
		if (event_onStart != null) {
			callJsEvent(event_onStart + "()");
		}
	}
	
	public void onStop() {
		if (flag_sensor) stopSensor();
		// geolocation_listeners
		for (int i = 0; i < geolocation_listeners.size(); i++) {
			GeoListener g = geolocation_listeners.get(i);
			if (g == null) continue;
			g.stop();
		}
		if (event_onStop != null) {
			callJsEvent(event_onStop + "()");
		}
	}
	
	public void onResume() {
		if (flag_sensor) startSensor();
		// geolocation_listeners
		for (int i = 0; i < geolocation_listeners.size(); i++) {
			GeoListener g = geolocation_listeners.get(i);
			if (g == null) continue;
			if (g.flagLive) g.start();
		}
		if (event_onResume != null) {
			callJsEvent(event_onResume + "()");
		}
	}
    
	// @see intent_startActivityForResult
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String param;
		if (requestCode == ACTIVITY_REQUEST_CODE_BARCODE && intent_startActivity_callback_barcode != null) {
			String contents = "";
			if (intent != null) {
				contents = intent.getStringExtra("SCAN_RESULT");
		        // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if (contents != null) contents = contents.replace("'", "\'");
			}
	        param = intent_startActivity_callback_barcode + "('" + contents +"')";
			callJsEvent(param);
		} else {
			if (intent_startActivity_callback == null) return;
			param = intent_startActivity_callback + "(" + requestCode + "," + resultCode + ")";
			callJsEvent(param);
		}
	}
	
    // menu selected
	public void onMenuItemSelected(int itemId) {
		callJsEvent(menu_item_callback_funcname + "(" + itemId + ")");
	}
	
	//---------------------------------------------------------------
	// Private method
	//---------------------------------------------------------------
	protected void stopSensor() {
		if (accel_listener != null) accel_listener.stop();
	}
	
	protected void startSensor() {
		if (accel_listener != null) accel_listener.start();
	}
	
    public void callJsEvent(String query) {
    	final String s = "javascript:" + query;
        waffle_activity.handler.post(new Runnable() {
			@Override
			public void run() {
				webview.loadUrl(s);
			}
		});
        // log(query);
    }

}
