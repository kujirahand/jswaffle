package com.kujirahand.jsWaffle.plugins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

import com.kujirahand.jsWaffle.WaffleActivity;
import com.kujirahand.jsWaffle.WaffleActivityFullScreen;
import com.kujirahand.jsWaffle.WaffleActivitySub;
import com.kujirahand.jsWaffle.model.WafflePlugin;
import com.kujirahand.jsWaffle.utils.IntentHelper;
import com.kujirahand.jsWaffle.utils.WaffleUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.Toast;

/*
 * Javaクラスのメソッドのみが登録される
 */
public class ABasicPlugin extends WafflePlugin
{
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
	 * Get Waffle Version Info
	 * @return version string
	 */
	public double getWaffleVersion() {
		return WaffleActivity.WAFFLE_VERSON;
	}
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
	 public boolean httpGet(final String url, final String callback_ok, final String callback_ng, final String tag) {
		 new Thread(new Runnable() {
				@Override
				public void run() {
					String result = WaffleUtils.httpGet(url);
					String query = "";
					if (result != null) {
						result = URLEncoder.encode(result);
						query = callback_ok + "('" + result + "'," + tag +  ")";
					} else {
						query = callback_ng + "('" + WaffleUtils.httpLastError + "'," + tag +  ")";
					}
					callJsEvent(query);
				}
			 }).start();
		 return true;
	 }
	 
	 public boolean httpPostJSON(final String url, final String json, final String callback, final int tag) {
		 new Thread(new Runnable() {
				@Override
				public void run() {
					String result = WaffleUtils.httpPostJSON(url, json);
					String query = callback + "(" + (result) + "," + tag +  ")";
					callJsEvent(query);
				}
			 }).start();
		 return true;
	 }
	 /**
	  * download file
	  * @param url
	  * @param filename
	  * @param callback
	  * @param tag
	  */
	 public void httpDownload(final String url, final String filename, final String callback, final int tag) {
		 new Thread(new Runnable() {
			@Override
			public void run() {
				boolean b = WaffleUtils.httpDownloadToFile(url, filename, waffle_activity);
				String query = callback + "(" + (b ? "true" : "false") + "," + tag +  ")";
				callJsEvent(query);
			}
		 }).start();
	 }
	 
	 public void clipboardSetText(String text) {
		 ClipboardManager cm = (ClipboardManager)waffle_activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		 cm.setText(text);
	 }
	 public String clipboardGetText() {
		 ClipboardManager cm = (ClipboardManager)waffle_activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		 return cm.getText().toString();
	 }
	 
	//---------------------------------------------------------------
	// Event Wrapper
	//---------------------------------------------------------------
	// @see intent_startActivityForResult
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String param;
		if (requestCode == ACTIVITY_REQUEST_CODE_BARCODE && intent_startActivity_callback_barcode != null) {
			String contents = "";
			String format = "text";
			if (intent != null) {
				contents = intent.getStringExtra("SCAN_RESULT");
		        format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if (contents != null) contents = URLEncoder.encode(contents);
			}
	        param = intent_startActivity_callback_barcode + "('" + contents + "','" + format + "')";
			callJsEvent(param);
		} else {
			if (intent_startActivity_callback == null) return;
			param = intent_startActivity_callback + "(" + requestCode + "," + resultCode + ")";
			callJsEvent(param);
		}
	}
		
	//---------------------------------------------------------------
	// Private method
	//---------------------------------------------------------------
    public void callJsEvent(String query) {
        waffle_activity.callJsEvent(query);
    }

}
