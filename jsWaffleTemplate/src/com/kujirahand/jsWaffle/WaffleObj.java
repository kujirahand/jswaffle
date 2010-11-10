package com.kujirahand.jsWaffle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
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
	public static double WAFFLE_VERSON = 0.2;
	//
	public static WaffleActivity waffle_activity = null;
	public static boolean flag_sensor = false;
	public AccelListener accel_listener = null;
	public static float accelX = 0;
	public static float accelY = 0;
	public static float accelZ = 0;
	//
	private WebView webview;
	
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
	public int geolocation_getCurrentPosition(String callback_ok, String callback_ng) {
		GeoListener geo_listener = new GeoListener(waffle_activity, this, 1);
		geo_listener.callback_success = callback_ok;
		geo_listener.callback_failed = callback_ng;
		geo_listener.flagLive = true;
		geo_listener.start();
		geolocation_listeners.add(geo_listener);
		return geolocation_listeners.size();
	}
	
	/**
	 * geolocation_watchPosition
	 * @param callback_ok
	 * @param callback_ng
	 * @return watchId
	 */
	public int geolocation_watchPosition(String callback_ok, String callback_ng) {
		GeoListener geo_listener = new GeoListener(waffle_activity, this, 0);
		geo_listener.callback_success = callback_ok;
		geo_listener.callback_failed = callback_ng;
		geo_listener.flagLive = true;
		geo_listener.start();
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
					i = new Intent(waffle_activity, FullScreenWaffleActivity.class);
				} else {
					i = new Intent(waffle_activity, SubWaffleActivity.class);
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
	public void intent_startActivity(Intent intent) {
		try {
			waffle_activity.startActivity(intent);
		} catch (Exception e) {
			log_error("activityError:" + e.getMessage());
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
	
	//---------------------------------------------------------------
	// Private method
	//---------------------------------------------------------------

	protected void stopSensor() {
		if (accel_listener != null) accel_listener.stop();
	}
	protected void startSensor() {
		if (accel_listener != null) accel_listener.start();
	}
	
	public void onStop() {
		if (flag_sensor) stopSensor();
		// geolocation_listeners
		for (int i = 0; i < geolocation_listeners.size(); i++) {
			GeoListener g = geolocation_listeners.get(i);
			if (g == null) continue;
			g.stop();
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

    // menu selected
	public void onMenuItemSelected(int itemId) {
		callJsEvent(menu_item_callback_funcname + "(" + itemId + ")");
	}
}
