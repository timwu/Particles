package com.timwu.Particles;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;

public class ParticleSimulator extends Activity implements SensorEventListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getParticleView().setDpi(metrics.xdpi, metrics.ydpi);
        // Setup accelerometer
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }
    
    private ParticleView getParticleView() {
    	return (ParticleView) findViewById(R.id.particle_view);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
		switch(getWindowManager().getDefaultDisplay().getRotation()) {
		case Surface.ROTATION_0:
			getParticleView().setGravity(-event.values[0], event.values[1]);
			break;
		case Surface.ROTATION_90:
			getParticleView().setGravity(event.values[1], event.values[0]);
			break;
		case Surface.ROTATION_180:
			getParticleView().setGravity(event.values[0], -event.values[1]);
			break;
		case Surface.ROTATION_270:
			getParticleView().setGravity(-event.values[1], -event.values[0]);
			break;
		}
	}
}