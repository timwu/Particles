package com.timwu.Particles;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class ParticleSimulator extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getParticleView().setDpi(metrics.xdpi, metrics.ydpi);
    }
    
    private ParticleView getParticleView() {
    	return (ParticleView) findViewById(R.id.particle_view);
    }
}