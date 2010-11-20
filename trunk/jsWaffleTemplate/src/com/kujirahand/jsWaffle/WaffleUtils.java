package com.kujirahand.jsWaffle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
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
			if (parent != null && parent.canWrite()) { parent.mkdirs(); }
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
			if (parent != null && parent.canWrite()) { parent.mkdirs(); }
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
	
}
