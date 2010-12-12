package com.kujirahand.jsWaffle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

/*
 * Javaクラスのメソッドのみが登録される
 */
public class WaffleObj
{
	public static double WAFFLE_VERSON = 1.12;
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
	public final static int DIALOG_TYPE_DEFAULT = 0;
	public final static int DIALOG_TYPE_YESNO = 0x10;
	public final static int DIALOG_TYPE_SELECT_LIST = 0x11;
	public final static int DIALOG_TYPE_CHECKBOX_LIST = 0x12;
	public final static int DIALOG_TYPE_DATE = 0x13;
	public final static int DIALOG_TYPE_TIME = 0x14;
	public final static int DIALOG_TYPE_PROGRESS = 0x15;
	public static int dialogType = DIALOG_TYPE_DEFAULT;
	public static String dialogTitle = null;
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
	/**
	 * Get Waffle Version Info
	 * @return version string
	 */
	public double getWaffleVersion() {
		return WAFFLE_VERSON;
	}
	
	/**
	 * Get android resource string
	 * @return resource string
	 */
	public String getResString(String name) {
		int id = waffle_activity.getResources().getIdentifier(name, "string", 
				waffle_activity.getPackageName());
		if (id == 0) {
			return "";
		}
		return waffle_activity.getResources().getString(id);
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
	public void setShakeCallback(String shake_callback_fn, String shake_end_callback_fn, double shake_freq, double shake_end_freq ) {
		if (accel_listener == null) {
			accel_listener = new AccelListener(waffle_activity, this);
		}
		if (shake_callback_fn == "") {
			stopSensor();
			flag_sensor = false;
			return;
		}
		// shake
		accel_listener.shake_callback_funcname = shake_callback_fn;
		accel_listener.shake_freq = shake_freq;
		// shake end
		accel_listener.shake_end_callback_funcname = shake_end_callback_fn;
		accel_listener.shake_end_freq = shake_end_freq;
		// start
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
		if (beep_tone != null) {
			if (beep_tone.isPlaying()) return;
			beep_tone.play();
		}
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
			FileOutputStream output = WaffleUtils.getOutputStream(filename, waffle_activity);
			if (output == null) return false;
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
			FileInputStream input = WaffleUtils.getInputStream(filename, waffle_activity);
			if (input == null) return null;
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
	public boolean fileExists(String filename) {
		File file = null;
		Uri uri = Uri.parse(filename);
		if (uri.getScheme() == null) {
			if (filename.startsWith("/sdcard/") || filename.startsWith("/data/")) {
				file = new File(filename);
			} else {
				file = waffle_activity.getFileStreamPath(filename);
			}
		}
		else { // file
			file = new File(uri.getPath());
		}
		return file.exists();
	}
	
	/**
	 * make directories
	 * @param path
	 * @return
	 */
	public boolean mkdir(String path) {
		File file = null;
		Uri uri = Uri.parse(path);
		if (uri.getScheme() == null) {
			if (path.startsWith("/sdcard/") || path.startsWith("/data/")) {
				file = new File(path);
			} else {
				file = waffle_activity.getFileStreamPath(path);
			}
		}
		else { // file
			file = new File(uri.getPath());
		}
		return file.mkdirs();
	}
	
	/**
	 * copy asset file
	 * @param assetsName
	 * @param savepath
	 * @return
	 */
	public boolean copyAssetsFile(String assetsName, String savepath) {
		return WaffleUtils.copyAssetsFile(waffle_activity, assetsName, savepath);
	}
	public boolean mergeSeparatedAssetsFile(String assetsName, String savepath) {
		return WaffleUtils.mergeSeparatedAssetsFile(waffle_activity, assetsName, savepath);
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
	
	
	ArrayList<DBHelper> dblist = null;
	/**
	 * open database
	 * @param dbname
	 * @return DBHelper object
	 */
	public DBHelper openDatabase(String dbname) {
		if (dblist == null) { dblist = new ArrayList<DBHelper>(); }
		DBHelper db = new DBHelper(waffle_activity, this);
		boolean b = db.openDatabase(dbname);
		if (!b) return null;
		dblist.add(db);
		return db;
	}
	public void executeSql(DBHelper db, String sql, String fn_ok, String fn_ng, String tag) {
		if (!(db instanceof DBHelper)) {
			log_error("executeSql : db is not DBHelper instance!!");
			return;
		}
		db.callback_result = fn_ok;
		db.callback_error = fn_ng;
		db.executeSql(sql, null, tag);
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
	public void setPromptType(int no, String title) {
		dialogType = no;
		dialogTitle = title;
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
	
	/**
	 * capture screen and save to file
	 * @param filename
	 * @param format png or jpeg
	 * @return
	 */
	public boolean snapshotToFile(String filename, String format) {
        webview.setDrawingCacheEnabled(true);
		Bitmap bmp = webview.getDrawingCache();
		if (bmp == null) {
			log_error("snapshot failed: bmp = null");
			return false;
		}
		// save to file
		Bitmap.CompressFormat fmt = Bitmap.CompressFormat.PNG;
		format = format.toLowerCase();
		if (format == "jpeg" || format == "image/jpeg") {
			fmt = Bitmap.CompressFormat.JPEG;
		}
		try {
			byte[] w = bmp2data(bmp, fmt, 80/*middle*/);
			writeDataFile(filename, w);
			return true;
		} catch (Exception e) {
			log_error("snapshot failed:" + e.getMessage());
			return false;
		}
	}
	 private static byte[] bmp2data(Bitmap src, Bitmap.CompressFormat format, int quality) {
		 ByteArrayOutputStream os=new ByteArrayOutputStream();
		 src.compress(format,quality,os);
		 return os.toByteArray();
	 }
	 private boolean writeDataFile(String filename, byte[] w) throws Exception {
		 OutputStream out = WaffleUtils.getOutputStream(filename, waffle_activity);
		 if (out == null) throw new Exception("FileOpenError:" + filename);
		 try {
			 out.write(w, 0, w.length);
			 out.close();
			 return true;
		 } catch (Exception e) {
			 out.close();
		 }
		 return false;
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
		// database
		if (dblist != null) {
			if (dblist.size() > 0) {
				for (int i = 0; i < dblist.size(); i++) {
					dblist.get(i).closeDatabase();
				}
			}
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
		// database
		if (dblist != null) {
			if (dblist.size() > 0) {
				for (int i = 0; i < dblist.size(); i++) {
					dblist.get(i).reopenDatabase();
				}
			}
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
        log(query);
    }

}
