package com.kujirahand.jsWaffle.plugins;

import java.util.Vector;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.kujirahand.jsWaffle.WaffleActivity;
import com.kujirahand.jsWaffle.model.WafflePlugin;

public class GPSPlugin extends WafflePlugin
{
	Vector<GeoListener> listeners = new Vector<GeoListener>();
	
	// javascript interface
	public int getCurrentPosition(String callback_ok, String callback_ng, boolean accuracy_fine) {
		GeoListener geo = new GeoListener(waffle_activity);
		listeners.add(geo);
		geo.callback_ok = callback_ok;
		geo.callback_ng = callback_ng;
		geo.report_count = 1;
		geo.start(accuracy_fine);
		return listeners.size();
	}
	
	public int watchPosition(String callback_ok, String callback_ng, boolean accuracy_fine) {
		GeoListener geo = new GeoListener(waffle_activity);
		listeners.add(geo);
		geo.callback_ok = callback_ok;
		geo.callback_ng = callback_ng;
		geo.report_count = 1;
		geo.start(accuracy_fine);
		return listeners.size();
	}
	
	public void clearWatch(int watchId) {
		try {
			int index = watchId - 1;
			if (listeners.size() <= index || index < 0) return;
			GeoListener i = listeners.get(index);
			if (i == null) return;
			i.flagLive = false;
			i.stop();
			listeners.set(index, null);
		} catch (Exception e) {
			waffle_activity.log_error("[GPS ERROR]clearWatch:" + e.getMessage());
		}
	}
	public void clearWatchAll() {
		for (int i = 0; i < listeners.size(); i++) {
			clearWatch(i + 1);
		}
	}
	
	public void onPause() {
		// geolocation_listeners
		for (int i = 0; i < listeners.size(); i++) {
			GeoListener g = listeners.get(i);
			if (g == null) continue;
			g.stop();
		}
	}
	
	public void onResume() {
		// geolocation_listeners
		for (int i = 0; i < listeners.size(); i++) {
			GeoListener g = listeners.get(i);
			if (g == null) continue;
			if (g.flagLive) g.start();
		}
	}
	
	public void onPageStarted() {
		clearWatchAll();
		listeners.clear();
	}
	
	public void onDestroy() {
		clearWatchAll();
		listeners.clear();
	}
}

class GeoListener implements LocationListener
{
	public long min_time = 1000; // msec 
	public float min_dist = 1f;  // 1m
	public long report_count = 0;
	public Boolean flagLive = false;
	private static LocationManager locman = null;
	public String callback_ok = "DroidWaffle._geolocation_fn_ok";
	public String callback_ng = "DroidWaffle._geolocation_fn_ng";
	public WaffleActivity waffle_activity;
	
	public GeoListener(WaffleActivity app) {
		this.waffle_activity = app;
	}
	
	public void start() {
		this.start(true);
	}
	
	public void start(boolean accuracy_fine) {
		// select provider
		Criteria crit = new Criteria();
		if (accuracy_fine) {
			crit.setAccuracy(Criteria.ACCURACY_FINE); // GPS
		} else {
			crit.setAccuracy(Criteria.ACCURACY_COARSE); // Faster
		}
		String providerName = getLocMan().getBestProvider(crit, true);
		LocationProvider p = null;
		if (providerName != null && providerName != "") {
			p = getLocMan().getProvider(providerName);
		}
		if (p == null) {
			waffle_activity.log_error("[GPS] no provider");
			waffle_activity.callJsEvent(callback_ng + "('no provider')");
			return;
		}
		// register
		waffle_activity.log("[GPS] set provider:" + providerName);
		getLocMan().requestLocationUpdates(providerName, min_time, min_dist, this);
	}

	public void stop() {
		try {
			getLocMan().removeUpdates(this);
		}
		catch (Exception e) {
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// Check Stop
		if (report_count > 0) {
			report_count--;
			if (report_count <= 0) {
				flagLive = false;
				stop();
			}
		}
		// Call Event
		String param = "" +
		location.getLatitude() + "," +
		location.getLongitude() + "," +
		location.getAltitude();
		String q = callback_ok + "(" + param + ")";
		waffle_activity.callJsEvent(q);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		case LocationProvider.AVAILABLE:
			waffle_activity.log("LocationStatus:AVAILABLE");
			break;
		case LocationProvider.OUT_OF_SERVICE:
			waffle_activity.log("LocationStatus:OUT_OF_SERVICE");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			waffle_activity.log("LocationStatus:TEMPORARILY_UNAVAILABLE");
			break;
		default:
			waffle_activity.log("LocationStatus:Unknown");
			break;
		}
	}
	
	private LocationManager getLocMan() {
		if (locman != null) {
			return locman;
		}
		locman = (LocationManager)waffle_activity.getSystemService(Activity.LOCATION_SERVICE);
		return locman;
	}
}
