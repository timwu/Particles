package com.timwu.Particles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ParticleView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "ParticleView";
	private static final float NANO_SECONDS_PER_SECOND = 1000000000.0f;
	private static final float GRAVITY_IN_INCHES = 32.2f * 12.0f;
	
	private ParticleViewLoop loop;
	private float xdpi;
	private float ydpi;
	
	public ParticleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loop = new ParticleViewLoop();
		getHolder().addCallback(this);
	}

	private class ParticleViewLoop extends Thread {
		private List<Particle> particles = Collections.synchronizedList(new ArrayList<Particle>());
		private long prevTick;
		private float curTimeslice;
		private float gravX, gravY;
		
		@Override
		public void run() {
			Log.i(TAG, "Starting particle simulator.");
//			Log.i(TAG, "Generating particles.");
//			addRandomParticles();
			
			prevTick = System.nanoTime();
			Log.i(TAG, "Starting with tick " + prevTick);
			setGravityAngle((float) (Math.PI / 2));
			while(!isInterrupted()) {
				synchronized (this) {
					tick(); // Update the clocking info
					doPhysics();
					doAnimation();
					doDraw();
				}
			}
		}
		
		private void doPhysics() {
			// Add a particle each time around
			sprayParticles(1, getWidth() / 2.0f, getHeight() * 0.1f, 300.0f, 
					(float) (3 * Math.PI / 2), (float) Math.PI / 4);
			
			// Physics states that any particle outside of the view, should be annihilated. I think.
			Iterator<Particle> pit = particles.iterator();
			while(pit.hasNext()) {
				Particle p = pit.next();
				if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight()) {
					pit.remove();
				}
			}
			
			// Gravity
			for (Particle p : particles) {
				p.accelerate(curTimeslice, gravX, gravY);
			}
		}
		
		private void doAnimation() {
			for(Particle p : particles) {
				p.move(curTimeslice);
			}
		}
		
		private void doDraw() {
			Canvas c = getHolder().lockCanvas();
			synchronized (getHolder()) {
				c.clipRect(0, 0, getWidth(), getHeight());
				c.drawColor(Color.BLACK);
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				for (Particle p : particles) {
					paint.setColor(p.color);
					c.drawCircle(p.x, p.y, p.r, paint);
				}
			}
			getHolder().unlockCanvasAndPost(c);
		}
		
		private void tick() {
			long curTick = System.nanoTime();
			curTimeslice = (curTick - prevTick) / NANO_SECONDS_PER_SECOND;
			prevTick = curTick;
		}
		
		private void sprayParticles(int n, float x, float y, float vMax, float angle, float angleWindow) {
			Random r = new Random();
			for (;n > 0; n--) {
				Particle p = new Particle();
				float v = vMax * r.nextFloat();
				float a = angle + (r.nextFloat() - 0.5f) * angleWindow;
				p.x = x;
				p.y = y;
				p.vx = (float) (Math.cos(a) * v);
				p.vy = (float) (Math.sin(a) * v);
				p.color = Color.CYAN;
				p.r = 2.0f;
				Log.i(TAG, "p " + p);
				particles.add(p);
			}
		}
		
		private synchronized void setGravityAngle(float angle) {
			gravX = (float) ((xdpi * GRAVITY_IN_INCHES / 100.0f) * Math.cos(angle));
			gravY = (float) (ydpi * GRAVITY_IN_INCHES / 100.0f * Math.sin(angle));
		}
	}
	
	private class Particle {
		private float x, y;
		private float vx, vy;
		private float r;
		private int color;
		
		private void move(float dt) {
			// Just doing a naive implementation where velocity will be
			// constant over the timeslice. Probably not too bad an approximation.
			x += vx * dt;
			y += vy * dt;
		}
		
		private void accelerate(float dt, float ax, float ay) {
			vx += dt * ax;
			vy += dt * ay;
		}
		
		public String toString() {
			return "(" + x + ", " + y + ") <" + vx + ", " + vy + ">";
		}
	}
	
	public void setDpi(float xdpi, float ydpi) {
		this.xdpi = xdpi;
		this.ydpi = ydpi;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int fmt, int width, int height) {
		Log.i(TAG, "Surface changed.");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "Starting drawing loop.");
		loop.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "Stopping drawing loop.");
		loop.interrupt();
	}
}
