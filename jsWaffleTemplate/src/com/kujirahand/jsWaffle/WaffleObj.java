package com.kujirahand.jsWaffle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import com.kujirahand.jsWaffle.plugin.WafflePlugin;

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
import android.widget.Toast;

/*
 * Javaクラスのメソッドのみが登録される
 */
public class WaffleObj extends WafflePlugin
{
	public static double WAFFLE_VERSON = 1.13;
	//
	public static int ACTIVITY_REQUEST_CODE_BARCODE = 0xFF0001;
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
	
	// callback string
	private String intent_startActivity_callback = null;
	private String intent_startActivity_callback_barcode = null;
	
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
	
	public void test(Object obj) {
		if (obj == null) {
			log("[test] null");
		} else {
			log("[test]"+obj.toString());
		}
	}
	public void testInt(int v) {
		log("[testInt]"+v);
	}
	public void testStr(String v) {
		log("[testStr]"+v);
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
	 * delete file
	 * @param filename
	 * @return boolean
	 */
	public boolean deleteFile(String filename) {
		File file = WaffleUtils.detectFile(filename, waffle_activity);
		if (file == null) return false;
		return file.delete();
	}
	/**
	 * get file size
	 * @param filename
	 * @return
	 */
	public long fileSize(String filename) {
		File file = WaffleUtils.detectFile(filename, waffle_activity);
		if (file == null) return 0;
		return file.length();
	}
	
	/**
	 * make directories
	 * @param path
	 * @return boolean
	 */
	public boolean mkdir(String path) {
		File file = WaffleUtils.detectFile(path, waffle_activity);
		if (file == null) return false;
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
		File dir = WaffleUtils.detectFile(path, waffle_activity);
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
		IntentHelper.request_code = -1;
		return _startIntent(url, false);
	}
	public boolean startIntentForResult(String url, String callback, int requestCode) {
		IntentHelper.request_code = requestCode;
		this.intent_startActivity_callback = callback;
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
	 
	 /**
	  * get data from url sync
	  */
	 public String httpGet(String url) {
		 return WaffleUtils.httpGet(url);
	 }
	 public String httpPostJSON(String url, String json) {
		 return WaffleUtils.httpPostJSON(url, json);
	 }
	 public void httpDownload(final String url, final String filename, final String callback) {
		 new Thread(new Runnable() {
			@Override
			public void run() {
				boolean b = WaffleUtils.httpDownloadToFile(url, filename, waffle_activity);
				String query = callback + "(" + (b ? "true" : "false") + ")";
				callJsEvent(query);
			}
		 }).start();
	 }
	 
	//---------------------------------------------------------------
	// Event Wrapper
	//---------------------------------------------------------------
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
    public void callJsEvent(String query) {
        waffle_activity.callJsEvent(query);
    }

}
