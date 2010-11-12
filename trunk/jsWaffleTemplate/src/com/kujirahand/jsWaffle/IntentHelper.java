package com.kujirahand.jsWaffle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class IntentHelper {
	
	public static boolean run(Context appContext, String url) {
		//TODO:URIの独自スキーマの定義
		// 各種  Intent の起動方法メモ
		// http://d.hatena.ne.jp/unagi_brandnew/20100309/1268115942
		// とても参考になる、今後追加すると良い
		if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("market://"))
		{
			Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			appContext.startActivity(browse);
			return true;
		}
		else if(url.startsWith("tel:"))
		{
			Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			appContext.startActivity(dial);
			return true;
		}
		else if(url.startsWith("sms:"))
		{  
			Uri smsUri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
			intent.setType("vnd.android-dir/mms-sms");
		  	appContext.startActivity(intent);
		  	return true;
	  	}
		else if(url.startsWith("geo:"))
		{  
			Uri mapUri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
		  	appContext.startActivity(intent);
		  	return true;
	  	}
		else if (url.startsWith("mailto:"))
		{
			// mailto:hoge@example.com?subject=test&body=hogehoge
			Uri mailUri = Uri.parse(url);
			MailToDecoder dec = new MailToDecoder(url);
			String subject = dec.subject;
			String body = dec.body;
			Intent intentMail = new Intent(Intent.ACTION_SENDTO, mailUri);
			intentMail.putExtra(Intent.EXTRA_SUBJECT, subject);
			intentMail.putExtra(Intent.EXTRA_TEXT, body);
			intentMail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//TODO:mailto attachfile
			appContext.startActivity(intentMail);
			return true;
		}
		else if (url.startsWith("file:")) {
			return runFile(appContext, url);
		}
		else if (url.startsWith("camera:")) {
			return runCamera(appContext, url, MediaStore.ACTION_IMAGE_CAPTURE);
		}
		else if (url.startsWith("video:")) {
			return runCamera(appContext, url, MediaStore.ACTION_VIDEO_CAPTURE);
		}
		return false;
	}
	
	private static boolean runCamera(Context appContext, String url, String mediaType) {
		try {
			url = url.replace("camera:", "file:");
			url = url.replace("video:",  "file:");
			//
			Uri saveUri = Uri.parse(url);
			Intent intent = new Intent();
			intent.setAction(mediaType);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, saveUri);
			appContext.startActivity(intent);
			return true;
		} catch (Exception e) {
			err("camera error:" + e.getMessage());
			return false;
		}
	}

	
	private static boolean runFile(Context appContext, String url) {
		Uri uriFile = Uri.parse(url);
		String ctype = getContentType(url);
		
		/*
		 * android_asset は別途処理することに
		// Check In android_asset
		String filepath = uriFile.getPath();
		if (filepath.startsWith("/android_asset")) {
			try {
				filepath = filepath.substring(15);
				InputStream ins = appContext.getAssets().open(filepath);
				if (ins == null) {
					throw new IOException("FileNotFound:" + url);
				}
				// copy to public area
				String tmpname = filepath.replaceAll("/", "_");
				File ext_dir = new File(Environment.getExternalStorageDirectory() + "/temp");
				if (!ext_dir.isDirectory()) {
					if (ext_dir.mkdir()) { // ここでエラーになる
						log("mkdir:" + ext_dir.getAbsolutePath());
					} else {
						err("mkdir failed:" + ext_dir.getAbsolutePath());
						return false;
					}
				}
				File outfile = new File(ext_dir.getAbsolutePath() + "/" + tmpname);
				try {
					FileOutputStream fos = new FileOutputStream(outfile); // ここでエラーになる
					copyFile(ins, fos);
					uriFile = Uri.parse("file:///" + outfile.getAbsolutePath());
				}
				catch(IOException e) {
					err("WriteError:" + outfile.getAbsolutePath() + ";" + e.getMessage());
					return false;
				}
			} catch(SecurityException e) {
				err("SecurityException:" + url + ";" + e.getMessage());
				return false;
			} catch(IOException e) {
				err("FileNotFound:" + url + ";" + e.getMessage());
				return false;
			}
		}
		*/
		// run
		File f = new File(uriFile.getPath());
		if (f.exists()) {
			if (ctype == null) return false;
			try {
				Intent intentFile = new Intent(Intent.ACTION_VIEW);
				intentFile.setDataAndType(uriFile, ctype);
				appContext.startActivity(intentFile);
				return true;
			} catch (Exception e) {
				err("[Intent]" + e.getMessage());
				return false;
			}
		}
		err("[Intent]FileNotFound:" + url);
		return false;
	}
	
	public static void copyFile(InputStream input , OutputStream output) 
		throws IOException {
		int DEFAULT_BUFFER_SIZE = 1024 * 4;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
		  output.write(buffer, 0, n);
		}
		input.close();
		output.close();
	}

	private static void err(String msg) {
		Log.e(WaffleActivity.LOG_TAG, msg);
	}
	private static void log(String msg) {
		Log.d(WaffleActivity.LOG_TAG, msg);
	}
	
	private static String getContentType(String url) {
		// Image
		if (url.endsWith(".png")) {
			return "image/png";
		}
		else if (url.endsWith(".jpg") || url.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		else if (url.endsWith(".gif") || url.endsWith(".gif")) {
			return "image/gif";
		}
		// Video
		if (url.endsWith(".3gp")||url.endsWith(".3gpp")) {
			return "video/3gpp";
		}
		else if (url.endsWith(".mpeg") || url.endsWith(".mpg") || url.endsWith(".mp4")) {
			return "video/mpeg";
		}
		// text
		else if (url.endsWith(".txt")) {
			return "text/plain";
		}
		else if (url.endsWith(".html")||url.endsWith(".htm")) {
			return "text/html";
		}
		// else
		else if (url.endsWith(".pdf")) {
			return "application/pdf";
		}
		else {
			log("not support type:" + url);
			return null;
		}
	}
}
