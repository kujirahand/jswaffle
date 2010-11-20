package com.kujirahand.jsWaffle;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class DBHelper {

	SQLiteDatabase myDb;
	String path;
	String txid = "";
	Context context;
	WaffleObj waffle_obj;
	
	public String callback_error = null;
	public String callback_result = null;
	
	public DBHelper(Context context, WaffleObj waffle_obj)
	{    	
		this.context = context;
		this.waffle_obj = waffle_obj;
	}
	
	public String getDBDir(String packageName)
	{
		return "/data/data/" + packageName + "/databases/";
	}
	
	public boolean openDatabase(String dbname)
	{
		// sd file?
		Uri uri = Uri.parse(dbname);
		File dbFile = null;
		try {
			String scheme = uri.getScheme();
			if (scheme == null) {
				if (dbname.startsWith("/sdcard/") || dbname.startsWith("/data/")) {
					dbFile = new File(dbname);
				} else {
					dbFile = context.getDatabasePath(dbname);
				}
			} else if (scheme.equals("file")) {
				dbFile = new File(uri.getPath());
			}
			else {
				return false;
			}
		} catch (Exception e) {
			Log.e(WaffleActivity.LOG_TAG, "DBOpenError: file path problem in " + dbname + ":" + e.getMessage());
			return false;
		}
		
		try {
			myDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
		} catch (Exception e) {
			Log.e(WaffleActivity.LOG_TAG, "DBOpenError: db problem in " + dbname + ":" + e.getMessage());
			return false;
		}
		// SQLiteDatabase.OPEN_READWRITE + SQLiteDatabase.CREATE_IF_NECESSARY);
		
		if (myDb == null) return false;
		return true;
	}
	
	public void executeSql(String query, String[] params, String tx_id)
	{
			try{
				txid = tx_id;
				Cursor myCursor = myDb.rawQuery(query, params);			
				processResults(myCursor);
			}
			catch (SQLiteException ex)
			{
				String err = ex.getMessage();
				err = err.replace("\"", "\\\"");
				Log.d(WaffleActivity.LOG_TAG, "DBError:" + err);
				
				String q = callback_error + "(\"" + err + "\")";
				waffle_obj.callJsEvent(q);
				
			}
	}
	
	public void processResults(Cursor cur)
	{		
		String key = "";
		String value = "";
		String resultString = "";
		if (cur.moveToFirst()) {
			 int colCount = cur.getColumnCount();
			 do {
				 resultString += "{";
				 for(int i = 0; i < colCount; ++i)
				 {
					 key  = cur.getColumnName(i);
					 value = cur.getString(i);
					 value = value.replace("\"", "\\\"");
					 resultString += " \"" + key + "\":\"" + value + "\"";
					 if (i != (colCount - 1))
						 resultString += ",";
				 }
				 resultString += "},";
			 } while (cur.moveToNext());
			 if (resultString != "") {
				 resultString = resultString.substring(0, resultString.length() - 1);
			 }
			 resultString = "[" + resultString + "]";
			 // myDb.close();
		}
		String q = callback_result + "(" + resultString + ")";
		waffle_obj.callJsEvent(q);
	}
	
	
}
