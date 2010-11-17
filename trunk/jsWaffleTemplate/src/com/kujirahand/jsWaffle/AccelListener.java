package com.kujirahand.jsWaffle;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class AccelListener implements SensorEventListener {

	SensorManager sensorMan;
	Context context;
	WaffleObj waffle_obj;
	public Boolean isLive = false;
	
	public static final int SHAKE_STATUS_READY = 0;
	public static final int SHAKE_STATUS_SHAKING = 1;
	
	public String sensour_callback_funcname = null;
	public String shake_callback_funcname = null;
	public String shake_end_callback_funcname = null;
	public double shake_freq = 20.0f;
	public double shake_end_freq = 8.0f;
	public int shake_status = SHAKE_STATUS_READY;
	
	public AccelListener(Context context, WaffleObj waffle_obj) {
		this.context = context;
		this.waffle_obj = waffle_obj;
		sensorMan = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	}
	
	public void start() {
		List<Sensor> sensors = sensorMan.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			Sensor se = sensors.get(0);
			isLive = sensorMan.registerListener(this, se, SensorManager.SENSOR_DELAY_GAME);
		}
		shake_status = SHAKE_STATUS_READY;
	}
	
	public void stop() {
		if (isLive) {
			sensorMan.unregisterListener(this);
			isLive = false;
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private long lastTime = 0;
    private float[] currentOrientationValues = {0.0f, 0.0f, 0.0f};
    private float[] currentAccelerationValues = {0.0f, 0.0f, 0.0f};

	@Override
	public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
        	WaffleObj.accelX = event.values[0];
        	WaffleObj.accelY = event.values[1];
        	WaffleObj.accelZ = event.values[2];
            // 傾き（ハイカット）
            currentOrientationValues[0] = event.values[0] * 0.1f + currentOrientationValues[0] * (1.0f - 0.1f);
            currentOrientationValues[1] = event.values[1] * 0.1f + currentOrientationValues[1] * (1.0f - 0.1f);
            currentOrientationValues[2] = event.values[2] * 0.1f + currentOrientationValues[2] * (1.0f - 0.1f);
            // 加速度（ローカット）
            currentAccelerationValues[0] = event.values[0] - currentOrientationValues[0];
            currentAccelerationValues[1] = event.values[1] - currentOrientationValues[1];
            currentAccelerationValues[2] = event.values[2] - currentOrientationValues[2];
            
            // 振ってる？　絶対値（あるいは２乗の平方根）の合計がいくつ以上か？
            float targetValue = 
                Math.abs(currentAccelerationValues[0]) + 
                Math.abs(currentAccelerationValues[1]) +
                Math.abs(currentAccelerationValues[2]);
            if(targetValue > shake_freq) {
            	/*
            	currentOrientationValues[0] = 0;
            	currentOrientationValues[1] = 0;
            	currentOrientationValues[2] = 0;
            	currentAccelerationValues[0] = 0;
            	currentAccelerationValues[1] = 0;
            	currentAccelerationValues[2] = 0;
            	*/
            	//振ったときの処理
            	if (shake_status == SHAKE_STATUS_READY) {
            		if (shake_callback_funcname != null) {
            			waffle_obj.callJsEvent(shake_callback_funcname + "()");
            			shake_status = SHAKE_STATUS_SHAKING;
            		}
            	}
            }
            else if(targetValue < shake_end_freq) {
            	// 振ってないときの処理
            	if (shake_status == SHAKE_STATUS_SHAKING) {
            		if (shake_end_callback_funcname != null) {
            			waffle_obj.callJsEvent(shake_end_callback_funcname + "()");
            			shake_status = SHAKE_STATUS_READY;
            		}
            	}
            }
            /*
            // 傾きは？３つの絶対値（あるいは２乗の平方根）のうちどれがいちばんでかいか？
            if(Math.abs(currentOrientationValues[0]) > 7.0f) {
                //orientation.setText("横");
            } else if(Math.abs(currentOrientationValues[1]) > 7.0f) {
                //orientation.setText("縦");
            } else if(Math.abs(currentOrientationValues[2]) > 7.0f) {
                //orientation.setText("水平");
            } else {
                //orientation.setText("");
            }
            */
            break;
        default:
        }
        // send event
        if (sensour_callback_funcname == null || sensour_callback_funcname == "") return;
        long now = SystemClock.uptimeMillis();
        long diff = now - lastTime;
        if (diff > 500) {
			String accel_str = sensour_callback_funcname + "(" + 
				WaffleObj.accelX + "," + WaffleObj.accelY + "," +
				WaffleObj.accelZ + ")";
			waffle_obj.callJsEvent(accel_str);
			lastTime = now;
        }
	}
	
}

