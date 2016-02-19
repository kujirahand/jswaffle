package com.kujirahand.jsWaffle.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.kujirahand.jsWaffle.WaffleActivity;
import com.kujirahand.jsWaffle.model.WafflePlugin;
import com.kujirahand.jsWaffle.utils.WaffleUtils;

import android.webkit.JavascriptInterface;

final public class StoragePlugin extends WafflePlugin {
    //---------------------------------------------------------------
    // preference method (public)
    //---------------------------------------------------------------

    /**
     * get public preference
     *
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
     *
     * @param key
     * @param value
     */
    @JavascriptInterface
    public void preferencePut(String key, String value) {
        Editor e = getPublicPreference().edit();
        e.putString(key, value);
        e.commit();
    }

    /**
     * Preferece get
     *
     * @param key
     * @param defaultValue
     * @return
     */
    @JavascriptInterface
    public String preferenceGet(String key, String defaultValue) {
        return getPublicPreference().getString(key, defaultValue);
    }

    /**
     * Preference exists key
     *
     * @param key
     * @return
     */
    @JavascriptInterface
    public boolean preferenceExists(String key) {
        return getPublicPreference().contains(key);
    }

    /**
     * Preference clear
     */
    @JavascriptInterface
    public void preferenceClear() {
        Editor e = getPublicPreference().edit();
        e.clear();
        e.commit();
    }

    /**
     * Preference remove
     */
    @JavascriptInterface
    public void preferenceRemove(String key) {
        Editor e = getPublicPreference().edit();
        e.remove(key);
        e.commit();
    }
    //---------------------------------------------------------------
    // preference method (private)
    //---------------------------------------------------------------

    /**
     * get private preference
     *
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
     *
     * @param key
     * @param value
     */
    @JavascriptInterface
    public void localStorage_put(String key, String value) {
        Editor e = getPrivatePreference().edit();
        e.putString(key, value);
        e.commit();
    }

    /**
     * local storage : Preferece get
     *
     * @param key
     * @param defaultValue
     * @return
     */
    @JavascriptInterface
    public String localStorage_get(String key, String defaultValue) {
        return getPrivatePreference().getString(key, defaultValue);
    }

    /**
     * local storage : Preference exists key
     *
     * @param key
     * @return
     */
    @JavascriptInterface
    public boolean localStorage_exists(String key) {
        return getPrivatePreference().contains(key);
    }

    /**
     * local storage : Preference clear
     */
    @JavascriptInterface
    public void localStorage_clear() {
        Editor e = getPrivatePreference().edit();
        e.clear();
        e.commit();
    }

    @JavascriptInterface
    public void localStorage_remove(String key) {
        Editor e = getPrivatePreference().edit();
        e.remove(key);
        e.commit();
    }

    @JavascriptInterface
    public String getStoragePath() {
        //return Environment.getExternalStorageDirectory().getAbsolutePath();
        return waffle_activity.getFilesDir().getAbsoluteFile().getAbsolutePath();
    }


    //---------------------------------------------------------------
    // file method
    //---------------------------------------------------------------

    /**
     * saveText
     *
     * @param filename
     * @param text
     * @retrun result
     */
    @JavascriptInterface
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
     *
     * @param filename
     * @return text
     */
    @JavascriptInterface
    public String loadText(String filename) {
        try {
            InputStream input = WaffleUtils.getInputStream(filename, waffle_activity);
            if (input == null) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            reader.close();
            return buf.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check file exists
     *
     * @param filename
     * @return true or false
     */
    @JavascriptInterface
    public boolean fileExists(String filename) {
        File file = null;
        Uri uri = Uri.parse(filename);
        try {
            if (uri.getScheme() == null) {
                if (filename.startsWith("/sdcard/") || filename.startsWith("/data/")) {
                    try {
                        file = new File(filename);
                    } catch (Exception e) {
                        return false;
                    }
                } else {
                    file = waffle_activity.getFileStreamPath(filename);
                }
            } else { // file
                file = new File(uri.getPath());
            }
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * delete file
     *
     * @param filename
     * @return boolean
     */
    @JavascriptInterface
    public boolean deleteFile(String filename) {
        File file = WaffleUtils.detectFile(filename, waffle_activity);
        if (file == null) return false;
        return file.delete();
    }

    /**
     * get file size
     *
     * @param filename
     * @return
     */
    @JavascriptInterface
    public long fileSize(String filename) {
        File file = WaffleUtils.detectFile(filename, waffle_activity);
        if (file == null) return 0;
        return file.length();
    }

    /**
     * make directories
     *
     * @param path
     * @return boolean
     */
    @JavascriptInterface
    public boolean mkdir(String path) {
        File file = WaffleUtils.detectFile(path, waffle_activity);
        if (file == null) return false;
        return file.mkdirs();
    }

    /**
     * copy asset file
     *
     * @param assetsName
     * @param savepath
     * @return
     */
    @JavascriptInterface
    public boolean copyAssetsFile(String assetsName, String savepath) {
        return WaffleUtils.copyAssetsFile(waffle_activity, assetsName, savepath);
    }

    @JavascriptInterface
    public boolean mergeSeparatedAssetsFile(String assetsName, String savepath) {
        return WaffleUtils.mergeSeparatedAssetsFile(waffle_activity, assetsName, savepath);
    }

    /**
     * get file list
     *
     * @param path
     * @return filenames (splitter ";")
     */
    @JavascriptInterface
    public String fileList(String path) {
        if (path.equals("undefined") || path == null) {
            path = "";
        }
        File dir = WaffleUtils.detectFile(path, waffle_activity);
        File[] files = dir.listFiles();
        String r = "";
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String f = files[i].getName();
                Log.d(WaffleActivity.LOG_TAG, f);
                r += f + ";";
            }
        }
        if (r != "") {
            r = r.substring(0, r.length() - 1);
        }
        return r;
    }

    /**
     * file copy
     */
    @JavascriptInterface
    public boolean fileCopy(String src, String des) {
        try {
            WaffleUtils.copyFileFromName(src, des, waffle_activity);
            return true;
        } catch (Exception e) {
            waffle_activity.log_error(e.getMessage());
            return false;
        }
    }

    /**
     * recording audio
     *
     * @fname *.3gp/*.amr
     */
    @JavascriptInterface
    public boolean audiorecStart(String fname) {

        if (mRecorder != null) {
            waffle_activity.log_error("audiorecStart() : already started.");
            return false;
        }

        int fmt = MediaRecorder.OutputFormat.DEFAULT;
        if (fname.endsWith(".3gp")) {
            fmt = MediaRecorder.OutputFormat.THREE_GPP;
        } else if (fname.endsWith(".amr")) {
            // fmt = MediaRecorder.OutputFormat.RAW_AMR;
        }
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(fmt);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mRecorder.setOutputFile(fname);

        try {
            mRecorder.prepare();
            mRecorder.start();
            return true;
        } catch (IllegalStateException e) {
            waffle_activity.log_error("audiorecStart() : state error : " + e.getMessage());
            return false;
        } catch (IOException e) {
            waffle_activity.log_error("audiorecStart() : io error : " + e.getMessage());
            return false;
        }
    }

    private MediaRecorder mRecorder = null;

    @JavascriptInterface
    public boolean audiorecStop(String fname) {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


