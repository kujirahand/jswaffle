package com.kujirahand.jsWaffle.plugins;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.kujirahand.jsWaffle.model.WafflePlugin;

public class StoragePlugin extends WafflePlugin
{
	//---------------------------------------------------------------
	// preference method (public)
	//---------------------------------------------------------------
	/**
	 * get public preference
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
	 * @param key
	 * @param value
	 */
	public void preferencePut(String key, String value) {
		Editor e = getPublicPreference().edit();
		e.putString(key, value);
		e.commit();
	}
	/**
	 * Preferece get
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String preferenceGet(String key, String defaultValue) {
		return getPublicPreference().getString(key, defaultValue);
	}
	/**
	 * Preference exists key
	 * @param key
	 * @return
	 */
	public boolean preferenceExists(String key) {
		return getPublicPreference().contains(key);
	}
	/**
	 * Preference clear
	 */
	public void preferenceClear() {
		Editor e = getPublicPreference().edit();
		e.clear();
		e.commit();
	}
	/**
	 * Preference remove
	 */
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
	 * @param key
	 * @param value
	 */
	public void localStorage_put(String key, String value) {
		Editor e = getPrivatePreference().edit();
		e.putString(key, value);
		e.commit();
	}
	/**
	 * local storage : Preferece get
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String localStorage_get(String key, String defaultValue) {
		return getPrivatePreference().getString(key, defaultValue);
	}
	/**
	 * local storage : Preference exists key
	 * @param key
	 * @return
	 */
	public boolean localStorage_exists(String key) {
		return getPrivatePreference().contains(key);
	}
	/**
	 * local storage : Preference clear
	 */
	public void localStorage_clear() {
		Editor e = getPrivatePreference().edit();
		e.clear();
		e.commit();
	}
	public void localStorage_remove(String key) {
		Editor e = getPublicPreference().edit();
		e.remove(key);
		e.commit();
	}
}
