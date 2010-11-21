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
	Context context;
	WaffleObj waffle_obj;
	String dbname;
	
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
	
	public void closeDatabase() {
		if (myDb != null) {
			myDb.close();
		}
	}
	
	public void reopenDatabase() {
		openDatabase(dbname);
	}
	
	public boolean openDatabase(String dbname)
	{
		this.dbname = dbname;
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
	
	public void executeSql(String query, String[] params, String tag)
	{
			try{
				Log.d(WaffleActivity.LOG_TAG, "SQL:" + query);
				Cursor myCursor = myDb.rawQuery(query, params);			
				processResults(myCursor, tag);
			}
			catch (SQLiteException ex)
			{
				String err = ex.getMessage();
				err = err.replace("\"", "\\\"");
				Log.d(WaffleActivity.LOG_TAG, "DBError:" + err);
				
				String q = callback_error + "(\"" + err + "\",\"" + tag + "\")";
				waffle_obj.callJsEvent(q);
				
			}
	}
	
	public void processResults(Cursor cur, String tag)
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
					 value = value.replace("\\", "\\\\");
					 value = value.replace("\"", "\\\"");
					 value = value.replace("\r", "\\r");
					 value = value.replace("\n", "\\n");
					 value = value.replace("\t", "\\t");
					 resultString += "\"" + key + "\":\"" + value + "\"";
					 //resultString += "\"" + key + "\":\"" + value + "\"";
					 if (i != (colCount - 1)) resultString += ",";
				}
				resultString += "},";
			} while (cur.moveToNext());
			if (resultString != "") {
				resultString = resultString.substring(0, resultString.length() - 1);
			}
			resultString = "[" + resultString + "]";
			cur.close();
			//resultString = java.net.URLEncoder.encode(resultString);
		} else {
			resultString = "null";
		}
		String q = callback_result + "("+resultString+","+tag+")";
		waffle_obj.callJsEvent(q);
	}
	
	
}
