package com.kujirahand.jsWaffle.plugins;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.kujirahand.jsWaffle.model.WafflePlugin;

import java.util.ArrayList;

final public class DatabasePlugin extends WafflePlugin {
    ArrayList<JWDBHelper> dblist = null;
    public static boolean isLive = false;

    /**
     * open database
     *
     * @param dbname
     * @return DBHelper object
     */
    @JavascriptInterface
    public int openDatabase(String dbname) {
        waffle_activity.log("openDatabase=" + dbname);
        if (dblist == null) {
            dblist = new ArrayList<JWDBHelper>();
        }
        JWDBHelper db = new JWDBHelper(waffle_activity);
        boolean b = db.openDatabase(dbname);
        if (!b) return -1;
        dblist.add(db);
        return (100 + dblist.size() - 1);
    }

    private JWDBHelper getDB(int dbId) {
        JWDBHelper db = null;
        dbId -= 100;
        if (dbId >= 0) {
            return dblist.get(dbId);
        }
        return null;
    }

    @JavascriptInterface
    public void executeSql(int dbId, final String sql, String fn_ok, String fn_ng, final String tag) {
        waffle_activity.log("executeSql=" + sql);
        final JWDBHelper db = getDB(dbId);
        if (!(db instanceof JWDBHelper)) {
            waffle_activity.log_error("executeSql : db is not DBHelper instance!!");
            return;
        }
        // If it is not live, do not execute SQL
        if (!isLive) return;
        db.callback_result = fn_ok;
        db.callback_error = fn_ng;
        //
        waffle_activity.runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    db.executeSql(sql, null, tag);
                }
            }
        );
    }

    @JavascriptInterface
    public String executeSqlSync(int dbId, String sql) {
        waffle_activity.log("executeSqlSync=" + sql);
        JWDBHelper db = getDB(dbId);
        if (!(db instanceof JWDBHelper)) {
            waffle_activity.log_error("executeSql : db is not DBHelper instance!!");
            return null;
        }
        String json = db.executeSqlSync(sql, null);
        if (json == null || json == "null") {
            return null;
        }
        return json;
    }

    @JavascriptInterface
    public String getDatabaseError(int dbId) {
        JWDBHelper db = getDB(dbId);
        if (!(db instanceof JWDBHelper)) {
            waffle_activity.log_error("executeSql : db is not DBHelper instance!!");
            return null;
        }
        return db.lastError;
    }

    @JavascriptInterface
    public void closeAll() {
        if (dblist == null) return;
        if (dblist.size() > 0) {
            for (int i = 0; i < dblist.size(); i++) {
                try {
                    JWDBHelper d = dblist.get(i);
                    if (d != null) d.closeDatabase();
                } catch (Exception e) {
                    Log.e("DatabasePlugin", "[ERROR]" + e.getMessage());
                }
            }
        }
    }

    @JavascriptInterface
    public void onResume() {
        isLive = true;
        if (dblist == null) return;
        try {
            if (dblist.size() > 0) {
                for (int i = 0; i < dblist.size(); i++) {
                    dblist.get(i).reopenDatabase();
                }
            }
        } catch (Exception e) {
        }
    }

    @JavascriptInterface
    public void onPause() {
        isLive = false;
        try {
            closeAll();
        } catch (Exception e) {
        }
    }

    @JavascriptInterface
    public void onPageStarted() {
        isLive = true;
        if (dblist == null) return;
        closeAll();
        dblist.clear();
    }

    @JavascriptInterface
    public void onDestroy() {
        isLive = false;
        if (dblist == null) return;
        closeAll();
        dblist.clear();
    }
}


