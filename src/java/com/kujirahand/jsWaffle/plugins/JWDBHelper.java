package com.kujirahand.jsWaffle.plugins;

import android.webkit.JavascriptInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;

import com.kujirahand.jsWaffle.WaffleActivity;

import java.io.File;

final public class JWDBHelper {
    public static int testID = 1000;

    SQLiteDatabase myDb;
    String path;
    WaffleActivity context;
    String dbname;

    public String callback_error = null;
    public String callback_result = null;

    public String lastError = null;

    public JWDBHelper(WaffleActivity context) {
        this.context = context;
    }

    @JavascriptInterface
    public String getDBDir(String packageName) {
        return "/data/data/" + packageName + "/databases/";
    }

    @JavascriptInterface
    public void closeDatabase() {
        if (myDb != null) {
            myDb.close();
        }
    }

    @JavascriptInterface
    public void reopenDatabase() {
        openDatabase(dbname);
    }

    @JavascriptInterface
    public boolean openDatabase(String dbname) {
        String sdcardpath = Environment.getExternalStorageDirectory().getPath();
        this.dbname = dbname;
        // sd file?
        Uri uri = Uri.parse(dbname);
        File dbFile = null;
        try {
            String scheme = uri.getScheme();
            if (scheme == null) {
                if (dbname.startsWith(sdcardpath) || dbname.startsWith("/data/")) {
                    dbFile = new File(dbname);
                } else {
                    dbFile = context.getDatabasePath(dbname);
                }
            } else if (scheme.equals("file")) {
                dbFile = new File(uri.getPath());
            } else {
                return false;
            }
        } catch (Exception e) {
            context.log_error("[DBOpenError] file path problem in " + dbname + ":" + e.getMessage());
            return false;
        }

        try {
            myDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (Exception e) {
            context.log_error("[DBOpenError] db problem in " + dbname + ":" + e.getMessage());
            return false;
        }
        // SQLiteDatabase.OPEN_READWRITE + SQLiteDatabase.CREATE_IF_NECESSARY);

        if (myDb == null) return false;
        return true;
    }

    @JavascriptInterface
    public void executeSql(String query, String[] params, String tag) {
        try {
            if (!myDb.isOpen()) reopenDatabase();
        } catch (Exception ex) {
            String err = ex.getMessage();
            lastError = err;
            err = err.replace("\"", "\\\"");
            context.log_error("[DBError] Reopen failed." + err + ":" + query);
        }
        try {
            if (!DatabasePlugin.isLive) return;
            Cursor myCursor = myDb.rawQuery(query, params);
            processResults(myCursor, tag, true);

        } catch (SQLiteException ex) {
            // SQLの失敗はしっかりJSに返してあげる
            String err = ex.getMessage();
            lastError = err;
            err = err.replace("\"", "\\\"");
            context.log_error("[DBSQLError]" + err + ":" + query);
            // JS
            if (DatabasePlugin.isLive) {
                String q = callback_error + "(\"" + err + "\",\"" + tag + "\")";
                context.callJsEvent(q);
                context.log_error("Execute JS SQL Error Handler");
            }
        } catch (Exception ex) {
            String err = ex.getMessage();
            lastError = err;
            err = err.replace("\"", "\\\"");
            context.log_error("[DBSQLError]" + err + ":" + query);
        }
    }

    @JavascriptInterface
    public String executeSqlSync(String query, String[] params) {
        try {
            if (!myDb.isOpen()) reopenDatabase();
            Cursor myCursor = myDb.rawQuery(query, params);
            return processResults(myCursor, "", false);
        } catch (SQLiteException ex) {
            lastError = ex.getMessage();
            return null;
        }
    }

    @JavascriptInterface
    public String processResults(Cursor cur, String tag, boolean flagCallJS) {
        String key = "";
        String value = "";
        String resultString = "";
        if (cur.moveToFirst()) {
            int colCount = cur.getColumnCount();
            do {
                resultString += "{";
                for (int i = 0; i < colCount; ++i) {
                    key = cur.getColumnName(i);
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
            cur.close();
        }
        if (flagCallJS) {
            String q = callback_result + "(" + resultString + "," + tag + ")";
            context.callJsEvent(q);
        }
        return resultString;
    }
}
