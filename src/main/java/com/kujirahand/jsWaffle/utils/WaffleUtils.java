package com.kujirahand.jsWaffle.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URL;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.kujirahand.jsWaffle.WaffleActivity;
import com.kujirahand.jsWaffle.utils.HTTPTask;

public class WaffleUtils {

    private static int BUFFSIZE = 1024 * 8;
    public static int http_timeout = 5000;

    /**
     * copy assets to external file
     *
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
            if (parent != null) {
                parent.mkdirs();
            }
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            Log.e(WaffleActivity.LOG_TAG, e.getMessage());
            return false;
        }
    }


    public static Uri checkFileUri(String filename) {
        Uri uri = Uri.parse(filename);
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (path.startsWith("/sdcard/")) {
            path = path.substring(8);
            File sdcard = Environment.getExternalStorageDirectory();
            uri = Uri.parse("file:/" + sdcard.getAbsolutePath() + "/" + path);
        }
        else if (path.startsWith("/Downloads")) {
            path = path.substring(10);
            File ff = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            uri = Uri.parse("file:/" + ff.getAbsolutePath() + path);
        }
        if ((scheme == null) && (path.startsWith("www/") || path.startsWith("/www/"))) {
            if (path.startsWith("/www")) {
                path = path.substring(1);
            }
            uri = Uri.parse("file:///android_asset/" + path);
        }
		/*
		if (path.startsWith("/android_asset/")) {
			// android_assets は特殊なので別途処理すべき
		}
		*/
        return uri;
    }

    public static File detectFile(String filename, Activity app) {
        File result = null;
        try {
            Uri uri = checkFileUri(filename);
            String scheme = uri.getScheme();
            String path = uri.getPath();
            WaffleActivity.mainInstance.log_warn("URI=" + uri);

            if (scheme == null) {
                result = app.getFileStreamPath(path);
            } else if (scheme.equals("file")) {
                result = new File(uri.getPath());
            } else {
                WaffleActivity.mainInstance.log_warn("Unknown scheme : " + filename);
                // error
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static FileOutputStream getOutputStream(String filename, Activity activity) {
        FileOutputStream output = null;
        try {
            File f = detectFile(filename, activity);
            if (f == null) {
                WaffleActivity.mainInstance.log_warn("[FileNotFound]" + filename);
                return null;
            }
            output = new FileOutputStream(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static InputStream getInputStream(String filename, Activity activity) {
        FileInputStream input = null;
        try {
            Uri uri = checkFileUri(filename);
            String path = uri.getPath();
            String scheme = uri.getScheme();
            // check assets
            if (path.startsWith("/android_asset/")) {
                path = path.substring(15);
                return activity.getAssets().open(path);
            }
            // check Contents Provider
            if (scheme != null && scheme.equals("content")) {
                return activity.getContentResolver().openInputStream(uri);
            }
            // cehck File Path
            File f = detectFile(filename, activity);
            if (f == null) {
                WaffleActivity.mainInstance.log_warn("[FileNotFound]" + filename);
                return null;
            }
            input = new FileInputStream(f);
        } catch (Exception e) {
        }
        return input;
    }

    /**
     * copy file
     *
     * @param src
     * @param des
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copyFileFromName(String src, String des, Activity app)
            throws FileNotFoundException, IOException {
        InputStream srcFile = getInputStream(src, app);
        if (srcFile == null) {
            throw new FileNotFoundException(src);
        }
        OutputStream desFile = getOutputStream(des, app);
        copyFileStream(srcFile, desFile);
    }

    public static void copyFile(File srcFile, File desFile)
            throws FileNotFoundException, IOException {
        try {
            File parent = desFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
        } catch (Exception e) {
        }
        //
        InputStream input = new FileInputStream(srcFile);
        OutputStream output = new FileOutputStream(desFile);
        copyFileStream(input, output);
    }

    public static void copyFileStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[BUFFSIZE];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        input.close();
        output.close();
    }

    public static String httpLastError = null;

    /**
     * get data from url
     *
     * @param url
     * @return get data
     */
    public static void httpGet(String url, String JSCallbackOk, String JSCallbackNg, int tag) {
        try {
            HTTPTask task = new HTTPTask();
            task.JSCallbackOk = JSCallbackOk;
            task.JSCallbackNg = JSCallbackNg;
            task.JSMethod = "string";
            task.Tag = tag;
            task.execute(new URL(url));
        } catch (MalformedURLException e) {
            httpLastError = e.getMessage();
            e.printStackTrace();
        } catch (Exception ee) {
            httpLastError = ee.getMessage();
            ee.printStackTrace();
        }
    }

    /**
     * http download
     *
     * @param url
     * @param filename
     * @return
     */
    public static boolean httpDownloadToFile(String url, String filename, String callbackOk, String callbackNg, int tag) {
        try {
            HTTPTask task = new HTTPTask();
            task.JSCallbackOk = callbackOk;
            task.JSCallbackNg = callbackNg;
            task.JSFilename = filename;
            task.JSFileStream = getOutputStream(filename, WaffleActivity.getInstance());
            if (task.JSFileStream == null) {
                WaffleActivity.getInstance().log("JSFileStream is NULL");
            }
            task.JSMethod = "file";
            task.Tag = tag;
            task.execute(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return false;
    }

    /**
     * post data to url
     *
     * @param sUrl  送信先URL
     * @param sJson 文字列に変換したJSONデータ
     */
    public static void httpPostJSON(String sUrl, String sJson, String callbackOk, String callbackNg, int tag) {
        try {
            HTTPTask task = new HTTPTask();
            task.JSCallbackOk = callbackOk;
            task.JSCallbackNg = callbackNg;
            task.JSMethod = "post";
            task.JSonStr = sJson;
            task.Tag = tag;
            task.execute(new URL(sUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
