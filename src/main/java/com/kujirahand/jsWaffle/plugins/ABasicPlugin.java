package com.kujirahand.jsWaffle.plugins;

import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;

import android.webkit.JavascriptInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;

import com.kujirahand.jsWaffle.WaffleActivity;
import com.kujirahand.jsWaffle.model.WafflePlugin;
import com.kujirahand.jsWaffle.utils.WaffleUtils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.webkit.WebView;
import android.widget.Toast;

/*
 * Javaクラスのメソッドのみが登録される
 */
final public class ABasicPlugin extends WafflePlugin {
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

    private ArrayList<MediaPlayer> playerList = new ArrayList<MediaPlayer>();

    //---------------------------------------------------------------
    // Interface
    //---------------------------------------------------------------

    /**
     * Get Waffle Version Info
     *
     * @return version string
     */
    @JavascriptInterface
    public double getWaffleVersion() {
        return WaffleActivity.WAFFLE_VERSON;
    }

    /**
     * Log
     */
    @JavascriptInterface
    public void log(String msg) {
        WaffleActivity.mainInstance.log(msg);
    }

    @JavascriptInterface
    public void log_error(String msg) {
        WaffleActivity.mainInstance.log_error(msg);
    }

    @JavascriptInterface
    public void log_warn(String msg) {
        WaffleActivity.mainInstance.log_warn(msg);
    }

    /**
     * Get android resource string
     *
     * @return resource string
     */
    @JavascriptInterface
    public String getResString(String name) {
        int id = waffle_activity.getResources().getIdentifier(name, "string",
                waffle_activity.getPackageName());
        if (id == 0) {
            return "";
        }
        return waffle_activity.getResources().getString(id);
    }

    /**
     * Get last error jsWaffle
     *
     * @return error string
     */
    @JavascriptInterface
    public String getLastError() {
        return WaffleActivity.mainInstance.last_error_str;
    }

    /**
     * beep
     */
    @JavascriptInterface
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
    @JavascriptInterface
    public void ring() {
        if (ring_tone == null) {
            Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ring_tone = RingtoneManager.getRingtone(waffle_activity, ringtone);
        }
        if (ring_tone != null) {
            if (ring_tone.isPlaying()) return;
            ring_tone.play();
        }
    }

    Ringtone ring_tone = null;

    @JavascriptInterface
    public void ring_stop() {
        if (ring_tone == null) {
            return;
        }
        if (ring_tone.isPlaying()) ring_tone.stop();
    }


    /**
     * vibrate
     *
     * @param msec
     */
    @JavascriptInterface
    public void vibrate(long msec) {
        long pattern = msec;
        if (pattern == 0) pattern = 500;
        Vibrator vibrator = (Vibrator) waffle_activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern);
    }

    /**
     * makeToast
     *
     * @param msg
     */
    @JavascriptInterface
    public void makeToast(String msg) {
        Toast.makeText(waffle_activity, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * play media file (for BGM)
     *
     * @param filename
     * @param loopMode
     * @return MediaPlayer
     * @paran audioType (music || ring)
     */
    @JavascriptInterface
    public int createPlayer(String soundfile, int loopMode, String audioType) {
        MediaPlayer mp = new MediaPlayer();
        try {
            if (audioType == "music") {
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else if (audioType == "ring" || audioType == null || audioType == "") {
                mp.setAudioStreamType(AudioManager.STREAM_RING);
            }
            //
            Uri uri = WaffleUtils.checkFileUri(soundfile);
            String path = uri.getPath();
            if (path.startsWith("/android_asset/")) {
                path = path.substring(15);
                AssetFileDescriptor fd = waffle_activity.getAssets().openFd(path);
                mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            } else {
                mp.setDataSource(path);
            }
            mp.prepare();
            mp.setLooping(loopMode == 1);
        } catch (IOException e) {
            log_error("[audio]" + e.getMessage() + "/" + soundfile);
            return -1;
        }
        // return id
        playerList.add(mp);
        return 100 + playerList.size() - 1;
    }

    @JavascriptInterface
    public void playPlayer(int mpId) {
        MediaPlayer mp = playerList.get(mpId - 100);
        if (mp == null) return;
        mp.seekTo(0);
        mp.start();
    }

    @JavascriptInterface
    public void stopPlayer(int mpId) {
        MediaPlayer mp = playerList.get(mpId - 100);
        if (mp == null) return;
        if (mp.isPlaying()) {
            mp.stop();
        }
    }

    @JavascriptInterface
    public boolean isPlayingSound(int mpId) {
        MediaPlayer mp = playerList.get(mpId - 100);
        if (mp == null) return false;
        return (mp.isPlaying());
    }

    @JavascriptInterface
    public void unloadPlayer(int mpId) {
        MediaPlayer mp = playerList.get(mpId - 100);
        if (mp == null) return;
        try {
            mp.release();
            mp = null;
        } catch (Exception e) {
        }
    }

    /**
     * play sound file (for Realtime play or loop) ... OGG is best!
     */
    private SoundPool pool = null;

    @JavascriptInterface
    public int loadSoundPool(String filename) {
        int res = -1;
        if (pool == null) pool = new SoundPool(5, AudioManager.STREAM_RING, 0);
        try {
            Uri uri = WaffleUtils.checkFileUri(filename);
            String path = uri.getPath();
            if (path.startsWith("/android_asset/")) {
                path = path.substring(15);
                AssetFileDescriptor fd = waffle_activity.getAssets().openFd(path);
                if (fd == null) throw new IOException("FileOpenError:" + path);
                res = pool.load(fd, 1);
            } else {
                res = pool.load(path, 1);
            }
            if (res >= 0) return res;
            log_error("[loadSoundPool]error:" + filename);
            return -1;
        } catch (Exception e) {
            log_error("[audio]" + e.getMessage());
            return -1;
        }
    }

    @JavascriptInterface
    public void playSoundPool(int id, int loop) {
        if (pool == null) {
            log_error("SoundPool not ready");
            return;
        }
        AudioManager am = (AudioManager) waffle_activity.getSystemService(Activity.AUDIO_SERVICE);
        int v = am.getStreamVolume(AudioManager.STREAM_RING);
        int max_v = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        float vf = (float) v / (float) max_v;
        // volume (0-1.0)
        log("volume=" + vf);
        pool.play(id, vf, vf, 1, loop, 1);
    }

    @JavascriptInterface
    public void stopSoundPool(int id) {
        if (pool == null) {
            log_error("SoundPool not ready");
            return;
        }
        pool.stop(id);
    }

    @JavascriptInterface
    public void unloadSoundPool(int id) {
        if (pool == null) {
            log_error("SoundPool not ready");
            return;
        }
        pool.unload(id);
    }

    /**
     * finish activity
     */
    @JavascriptInterface
    public void finish() {
        try {
            waffle_activity.finish();
        } catch (Exception e) {
            log("jswaffle.finish()");
        }
    }

    /**
     * Add menu
     */
    @JavascriptInterface
    public void setMenuItem(int itemNo, boolean visible, String title, String iconName) {
        waffle_activity.setMenuItem(itemNo, visible, title, iconName);
    }

    public static String menu_item_callback_funcname = null;

    @JavascriptInterface
    public void setMenuItemCallback(String callback_fn) {
        menu_item_callback_funcname = callback_fn;
    }

    /**
     * Dialog
     */
    @JavascriptInterface
    public void setPromptType(int no, String title) {
        dialogType = no;
        dialogTitle = title;
    }

    /**
     * capture screen and save to file
     *
     * @param filename
     * @param format   png or jpeg
     * @return
     */
    @JavascriptInterface
    public boolean snapshotToFile(String filename, String format) {
        // snapshot
        Bitmap bmp = null;
        try {
            webview.setDrawingCacheEnabled(true);
            bmp = Bitmap.createBitmap(webview.getDrawingCache());
            webview.setDrawingCacheEnabled(false);
        } catch (Exception e) {
            log_error("snapshot failed: " + e.getMessage());
            return false;
        }
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
            boolean b = writeDataFile(filename, w);
            bmp.recycle(); // recycle !!
            return b;
        } catch (Exception e) {
            log_error("snapshot failed:" + e.getMessage());
            return false;
        }
    }

    private static byte[] bmp2data(Bitmap src, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);
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
    @JavascriptInterface
    public boolean httpGet(final String url, final String callback_ok, final String callback_ng, final int tag) {
        waffle_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WaffleUtils.httpGet(url, callback_ok, callback_ng, tag);
            }
        });
        return true;
    }

    @JavascriptInterface
    public boolean httpPostJSON(final String url, final String json, final String callbackOk, final String callbackNg, final int tag) {
        waffle_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WaffleUtils.httpPostJSON(url, json, callbackOk, callbackNg, tag);
            }
        });
        return true;
    }

    /**
     * download file
     *
     * @param url
     * @param filename
     * @param callbackOk
     * @param callbackNg
     * @param tag
     */
    @JavascriptInterface
    public void httpDownload(final String url, final String filename, final String callbackOk, final String callbackNg, final int tag) {
        waffle_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WaffleUtils.httpDownloadToFile(url, filename, callbackOk, callbackNg, tag);
            }
        });
    }

    @JavascriptInterface
    public void showMenu() {
        waffle_activity.openOptionsMenu();
    }

    @JavascriptInterface
    public void clipboardSetText(String text) {
        ClipboardManager cm = (ClipboardManager) waffle_activity.getSystemService(Activity.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    @JavascriptInterface
    public String clipboardGetText() {
        ClipboardManager cm = (ClipboardManager) waffle_activity.getSystemService(Activity.CLIPBOARD_SERVICE);
        return cm.getText().toString();
    }

    static public boolean flagStopBackKey = false;

    @JavascriptInterface
    public boolean stopBackKey() {
        ABasicPlugin.flagStopBackKey = true;
        return true;
    }


    @JavascriptInterface
    public void setKeyboardVisible(boolean b) {
        final Handler h = new Handler();
        final boolean bShow = b;
        final WebView webview = this.webview;
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                InputMethodManager manager =
                        (InputMethodManager)waffle_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (bShow) {
                    manager.showSoftInput(webview, InputMethodManager.SHOW_IMPLICIT);
                    log("show keybd");
                 } else {
                    manager.hideSoftInputFromWindow(webview.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    log("hide keybd");
                }
            }
        };
        h.postDelayed(run, 1500);
    }

    //---------------------------------------------------------------
    // Event Wrapper
    //---------------------------------------------------------------

    private Hashtable<String, EventList> eventList = null;

    @JavascriptInterface
    public void addEventListener(String eventName, String callback, int tag) {
        if (eventList == null) eventList = new Hashtable<String, EventList>();
        EventList e = eventList.get(eventName);
        if (e == null) {
            e = new EventList();
            eventList.put(eventName, e);
        }
        EventListItem i = new EventListItem();
        i.callback = callback;
        i.tag = tag;
        e.list.add(i);
    }

    @JavascriptInterface
    public void removeEventListener(String eventName, int tag) {
        if (eventList == null) return;
        EventList e = eventList.get(eventName);
        if (e == null) return;
        for (int i = 0; i < e.list.size(); i++) {
            EventListItem item = e.list.get(i);
            if (item.tag == i) {
                e.list.remove(i);
                break;
            }
        }
    }

    private void doEventListener(String eventName, String paramStr) {
        if (eventList == null) return;
        EventList e = eventList.get(eventName);
        if (e == null) return;
        for (int i = 0; i < e.list.size(); i++) {
            EventListItem item = e.list.get(i);
            String args = "";
            if (paramStr != null) {
                args = item.tag + "," + paramStr;
            } else {
                args = Integer.toString(item.tag);
            }
            String p = String.format("%s(%s)", item.callback, args);
            callJsEvent(p);
        }
    }

    @JavascriptInterface
    @Override
    public void onPause() {
        doEventListener("pause", null);
    }

    @JavascriptInterface
    @Override
    public void onResume() {
        doEventListener("resume", null);
    }

    @JavascriptInterface
    @Override
    public void onDestroy() {
        doEventListener("destroy", null);
    }

    @JavascriptInterface
    @Override
    public void onPageStarted(String url) {
        // remove listener
        if (eventList != null) {
            eventList.clear();
            eventList = null;
        }
    }

    @JavascriptInterface
    @Override
    public void onPageFinished(String url) {
        doEventListener("pageFinished", url);
    }

    //---------------------------------------------------------------
    // Private method
    //---------------------------------------------------------------
    @JavascriptInterface
    public void callJsEvent(String query) {
        waffle_activity.callJsEvent(query);
    }

    class EventListItem {
        String callback;
        int tag;
    }

    class EventList {
        ArrayList<EventListItem> list = new ArrayList<EventListItem>();
    }
}


