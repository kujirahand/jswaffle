package com.kujirahand.jsWaffle;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class GeoListener implements LocationListener{

	Context context;
	WaffleObj waffle_obj;
	LocationManager locationMan;
	//PowerManager powerMan;
	//WakeLock wakelock = null;
	public String callback_success	= "DroidWaffle._geolocation_fn_ok";
	public String callback_failed	= "DroidWaffle._geolocation_fn_ng";
	public long min_time = 1000; // msec 
	public float min_dist = 1f;  // 1m
	private long report_count = 0;
	public Boolean flagLive = false;
	private boolean accuracy_fine = true;
	
	public GeoListener(Context context, WaffleObj waffle_ogj, long report_count) {
		this.context = context;
		this.waffle_obj = waffle_ogj;
		this.report_count = report_count;
		//
		locationMan = (LocationManager)context.getSystemService(Activity.LOCATION_SERVICE);
		//powerMan = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		
	}
	
	public void start() {
		this.start(accuracy_fine);
	}
	
	public void start(boolean accuracy_fine) {
		this.accuracy_fine = accuracy_fine;
		// provider
		Criteria crit = new Criteria();
		if (accuracy_fine) {
			crit.setAccuracy(Criteria.ACCURACY_FINE); // More accurate GPS
		} else {
			crit.setAccuracy(Criteria.ACCURACY_COARSE); // Faster, no GPS fix
		}
		String providerName = locationMan.getBestProvider(crit, true);
		
		LocationProvider p = null;
		p = locationMan.getProvider(providerName);
		if (p == null) {
			if (locationMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				p = locationMan.getProvider(LocationManager.GPS_PROVIDER);
			}
		}
		if (p == null) {
			if (locationMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				p = locationMan.getProvider(LocationManager.NETWORK_PROVIDER);
			}
		}
		if (p == null) {
			// failed
			waffle_obj.callJsEvent(callback_failed + "('no provider')");
			return;
		}
		// set wakelock
		// wakelock = powerMan.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		// request
		Log.d(WaffleActivity.LOG_TAG, "LocationProvider=" + p.getName());
		locationMan.requestLocationUpdates(p.getName(), min_time, min_dist, this);
		/*
		Location loc = locationMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		long now = System.currentTimeMillis();
		if (now - loc.getTime() < 30 * 1000) {
			onLocationChanged(loc);
		}
		*/
	}
	
	public void stop() {
		try {
			locationMan.removeUpdates(this);
			// if (wakelock != null) wakelock.release();
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
		waffle_obj.log("onLocationChanged:" + param);
		String q = callback_success + "(" + param + ")";
		waffle_obj.callJsEvent(q);
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
			Log.d(WaffleActivity.LOG_TAG, "LocationStatus:AVAILABLE");
			break;
		case LocationProvider.OUT_OF_SERVICE:
			//String q = callback_success + "('out of service')";
			//waffle_obj.callJsEvent(q);
			//stop();
			Log.d(WaffleActivity.LOG_TAG, "LocationStatus:OUT_OF_SERVICE");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.d(WaffleActivity.LOG_TAG, "LocationStatus:TEMPORARILY_UNAVAILABLE");
			break;
		default:
			Log.d(WaffleActivity.LOG_TAG, "LocationStatus:Unknown");
			break;
		}
	}

}
