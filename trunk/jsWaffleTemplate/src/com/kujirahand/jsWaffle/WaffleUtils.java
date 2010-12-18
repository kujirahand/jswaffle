package com.kujirahand.jsWaffle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

public class WaffleUtils {
	
	private static int BUFFSIZE = 1024 * 16;
	
	/**
	 * copy assets to external file 
	 * @param app
	 * @param assetsName
	 * @param savepath
	 * @return
	 */
	public static boolean copyAssetsFile(Activity app, String assetsName, String savepath) {
		FileOutputStream fo;
		InputStream inp;
		try {
			File f = new File(savepath);
			File parent = f.getParentFile();
			if (parent != null) { parent.mkdirs(); }
			fo = new FileOutputStream(f);
		} catch (IOException e) {
			Log.e(WaffleActivity.LOG_TAG, "copyAssetsFile() savepath could not open:" + e.getMessage() + ",file=" + savepath);
			return false;
		}
		AssetManager am = app.getResources().getAssets();
		try {
			byte[] buf = new byte[BUFFSIZE];
			inp = am.open(assetsName);
			while (true) {
				int sz = inp.read(buf, 0, BUFFSIZE);
				if (sz <= 0) break;
				fo.write(buf, 0, sz);
			}
			fo.close();
			return true;
		}catch(IOException e) {
			Log.e(WaffleActivity.LOG_TAG, e.getMessage());
			return false;
		}
	}
	
	public static boolean mergeSeparatedAssetsFile(Activity app, String assetsName, String savepath) {
		/*
		 * In assets dir,
		 *   input  : name.1 name.2 name.3
		 *   output : savepath
		 */
		
		FileOutputStream fo;
		InputStream inp;
		try {
			File f = new File(savepath);
			File parent = f.getParentFile();
			if (parent != null) { 
				parent.mkdirs();
			}
			fo = new FileOutputStream(f);
		} catch (IOException e) {
			Log.e(WaffleActivity.LOG_TAG, "mergeSeparatedAssetsFile() savepath could not open:" + e.getMessage() + ",file=" + savepath);
			return false;
		}
		AssetManager am = app.getResources().getAssets();
		try {
			byte[] buf = new byte[BUFFSIZE];
			
			for (int no = 1; no < 999; no++) {
				String fname = assetsName + "." + no;
				try {
					inp = am.open(fname);
					if (inp == null) break;
				} catch (IOException e) {
					break;
				}
				while (true) {
					int sz = inp.read(buf, 0, BUFFSIZE);
					if (sz <= 0) break;
					fo.write(buf, 0, sz);
				}
			}
			
			fo.close();
			return true;
		}catch(IOException e) {
			Log.e(WaffleActivity.LOG_TAG, e.getMessage());
			return false;
		}
	}
	
	public static FileOutputStream getOutputStream(String filename, Activity activity) {
		FileOutputStream output = null;
		try {
			Uri uri = Uri.parse(filename);
			String scheme = uri.getScheme();
			if (scheme == null) {
				output = activity.openFileOutput(filename, Context.MODE_PRIVATE);
			}
			else if (scheme.equals("file")) {
				File f = new File(uri.getPath());
				output = new FileOutputStream(f);
			}
			else {
				// error
			}
		} catch (Exception e) {
		}
		return output;
	}
	public static FileInputStream getInputStream(String filename, Activity activity) {
		FileInputStream input = null;
		try {
			Uri uri = Uri.parse(filename);
			String scheme = uri.getScheme();
			if (scheme == null) {
				input = activity.openFileInput(filename);
			}
			else if (scheme.equals("file")) {
				File f = new File(uri.getPath());
				input = new FileInputStream(f);
			}
			else {
			}
		} catch (Exception e) {
		}
		return input;
	}
	
	/**
	 * get data from url
	 * @param url
	 * @return get data
	 * @see http://se-suganuma.blogspot.com/2010/02/androidhttpclienthttpgetjson.html
	 */
	public static String httpGet(String url) {
		HttpClient objHttp = new DefaultHttpClient();
		HttpParams params = objHttp.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 1000); //接続のタイムアウト
		HttpConnectionParams.setSoTimeout(params, 1000); //データ取得のタイムアウト
		String sReturn = "";
	    try {
	    	HttpGet objGet   = new HttpGet(url);
	        HttpResponse objResponse = objHttp.execute(objGet);
	        if (objResponse.getStatusLine().getStatusCode() < 400){
	            InputStream objStream = objResponse.getEntity().getContent();
	            InputStreamReader objReader = new InputStreamReader(objStream);
	            BufferedReader objBuf = new BufferedReader(objReader);
	            StringBuilder objJson = new StringBuilder();
	            String sLine;
	            while((sLine = objBuf.readLine()) != null){
	            	objJson.append(sLine);
	            }
	            sReturn = objJson.toString();
	            objStream.close();
	        }
	    } catch (IOException e) {
	    	return null;
	    }	
	    return sReturn;
	}
	
	/**
	 * post data to url
	 * @param sUrl 送信先URL
	 * @param sJson 文字列に変換したJSONデータ
	 * @return
	 */
	public static String httpPostJSON(String sUrl, String sJson) {
		HttpClient objHttp = new DefaultHttpClient();
		String sReturn = "";
	    try {
	    	HttpPost objPost   = new HttpPost(sUrl);
	    	List<NameValuePair> objValuePairs = new ArrayList<NameValuePair>(2);  
	    	objValuePairs.add(new BasicNameValuePair("json", sJson));
	        objPost.setEntity(new UrlEncodedFormEntity(objValuePairs, "UTF-8"));
	        HttpResponse objResponse = objHttp.execute(objPost);
	        if (objResponse.getStatusLine().getStatusCode() < 400){
	            InputStream objStream = objResponse.getEntity().getContent();
	            InputStreamReader objReader = new InputStreamReader(objStream);
	            BufferedReader objBuf = new BufferedReader(objReader);
	            StringBuilder objJson = new StringBuilder();
	            String sLine;
	            while((sLine = objBuf.readLine()) != null){
	            	objJson.append(sLine);
	            }
	            sReturn = objJson.toString();
	            objStream.close();
	        }
	    } catch (IOException e) {
	    	return null;
	    }	
	    return sReturn;
	}
}
