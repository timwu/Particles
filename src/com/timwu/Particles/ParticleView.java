package com.timwu.Particles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.timwu.Particles.math.Particle;
import com.timwu.Particles.math.Segment;
import com.timwu.Particles.math.Physics;
import com.timwu.Particles.math.Vector2d;
import com.timwu.Particles.math.force.IForceField;
import com.timwu.Particles.math.force.SimpleForce;
import com.timwu.Particles.math.force.SuckForce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.GestureDetector.SimpleOnGestureListener;

public class ParticleView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "ParticleView";
	
	private ParticleViewLoop loop;
	private float xdpi;
	private float ydpi;
	private GestureDetector gestureDetector;
	
	public ParticleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loop = new ParticleViewLoop();
		getHolder().addCallback(this);
		gestureDetector = new GestureDetector(context, new TouchListener());
	}

	private class ParticleViewLoop extends Thread {
		// Objects
		private List<Particle> particles = new ArrayList<Particle>();
		private List<Segment> segments = new ArrayList<Segment>();
		
		// Physical state
		private Vector2d sprayerPos;
		private SimpleForce gravity;
		private List<IForceField> forces = new ArrayList<IForceField>();
		
		// Timing
		private long prevTick;
		private float curTimeslice;
		
		// Segment drawing
		private Segment currentLine;
		
		// Touch controls
		private boolean touchDown;
		public boolean singleTap;
		private MotionEvent downEvent;
		private MotionEvent scrollEvent;
		
		// FPS Display
		private FPSCounter fps;
		
		private boolean running = false;
		private Random r = new Random();
		
		private void init() {
			Log.i(TAG, "Starting particle simulator.");
			prevTick = System.nanoTime();
			Log.i(TAG, "Starting with tick " + prevTick);
			
			// Tick once to initialize to prime the FPS counter
			tick();
			fps = new FPSCounter(curTimeslice);
			
			// Place the sprayer
			sprayerPos = new Vector2d(getWidth() / 2, getHeight() * 0.2f);
			
			// Setup gravity
			gravity = new SimpleForce();
			gravity.setScale(xdpi * Physics.G_INCHES / 100.0f / Physics.G_METERS,
					         ydpi * Physics.G_INCHES / 100.0f  / Physics.G_METERS);
			gravity.setDirection(0.0f, Physics.G_METERS);
			
			// Start running
			running = true;
		}
		
		@Override
		public void run() {
			init();
			while(running) {
				tick(); // Update the clocking info
				doInput();
				doPhysics();
				doAnimation();
				doDraw();
			}
			particles.clear();
		}
		
		private void doInput() {
			if (singleTap) {
				SuckForce sf = new SuckForce(downEvent.getX(), downEvent.getY(), 150.0f, 1500.0f, 1.0f);
				forces.add(sf);
				singleTap = false;
			}
			if (!touchDown) {
				currentLine = null;
				return;
			}
			if (currentLine == null) {
				currentLine = new Segment(downEvent.getX(), downEvent.getY(), scrollEvent.getX(), scrollEvent.getY());
				segments.add(loop.currentLine);
			}
			currentLine.setEnd(scrollEvent.getX(), scrollEvent.getY());
		}
		
		private void doPhysics() {
			// Add a particle each time around
			sprayParticles(1, sprayerPos, 300.0f, 
					Physics.PI * 3 / 2, Physics.PI / 4);
			
			// Physics states that any particle outside of the view, should be annihilated. I think.
			Iterator<Particle> pit = particles.iterator();
			while(pit.hasNext()) {
				Particle p = pit.next();
				if (p.getPos().getX() < 0 || p.getPos().getX() > getWidth() || 
						p.getPos().getY() < 0 || p.getPos().getY() > getHeight()) {
					pit.remove();
				}
			}
			
			// Update the forces
			Iterator<IForceField> fit = forces.iterator();
			while(fit.hasNext()) {
				IForceField force = fit.next();
				force.update(curTimeslice);
				if (force.getPurge()) {
					fit.remove();
				}
			}
						
			for (Particle p : particles) {
				// Apply accelerations to the particle
				p.accelerate(curTimeslice, gravity.getForce(p.getPos()));
				for (IForceField force : forces) {
					p.accelerate(curTimeslice, force.getForce(p.getPos()));
				}
				
				// Do bouncing off segments and move
				float particleTimeslice = curTimeslice;
				while (particleTimeslice > Physics.FUDGE) {
					float tImpact = particleTimeslice;
					Segment bounceSegment = null;
					for (Segment segment : segments) {
						float segImpact = p.impactTime(segment);
						if (segImpact < tImpact) {
						bounceSegment = segment;
						tImpact = segImpact;
						}
					}
					if (bounceSegment != null) {
						p.move(tImpact);
						p.bounce(bounceSegment);
						particleTimeslice -= tImpact;
					} else {
						p.move(particleTimeslice);
						particleTimeslice = 0;
					}
				}
			}
		}
		
		private void doAnimation() {
		}
		
		private void doDraw() {
			Canvas c = getHolder().lockCanvas();
			if (c == null) {
				Log.i(TAG, "Couldn't get a canvas to draw on.");
				return;
			}
			synchronized (getHolder()) {
				c.clipRect(0, 0, getWidth(), getHeight());
				c.drawColor(Color.BLACK);
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				for (Particle p : particles) {
					paint.setColor(p.getColor());
					c.drawCircle(p.getPos().getX(), p.getPos().getY(), p.getR(), paint);
				}
				for (Segment l : segments) {
					paint.setColor(l.getColor());
					c.drawLine(l.getStart().getX(), l.getStart().getY(), l.getEnd().getX(), l.getEnd().getY(), paint);
				}
				paint.setColor(Color.WHITE);
				paint.setTextSize(10.0f);
				c.drawText(fps.getFps(curTimeslice), 0, 10, paint);
			}
			getHolder().unlockCanvasAndPost(c);
		}
		
		private void tick() {
			long curTick = System.nanoTime();
			curTimeslice = (curTick - prevTick) / Physics.NANO_SECONDS_PER_SECOND;
			prevTick = curTick;
		}
		
		private void sprayParticles(int n, Vector2d pos, float speedMax, float angleMid, float angleRange) {
			for (;n > 0; n--) {
				float speed = speedMax * r.nextFloat();
				float a = angleMid + (r.nextFloat() - 0.5f) * angleRange;
				Vector2d v = new Vector2d((float) (speed * Math.cos(a)), (float) (speed * Math.sin(a)));
				particles.add(new Particle(pos, v, 2.0f, Color.CYAN));
			}
		}
		
		private void setGravity(float ax, float ay) {
			gravity.setDirection(ax, ay);
		}
	}
		
	private class TouchListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent down, MotionEvent scroll,
				float distanceX, float distanceY) {
			loop.downEvent = down;
			loop.scrollEvent = scroll;
			loop.touchDown = true;
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			loop.singleTap = true;
			loop.downEvent = e;
			return true;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			loop.touchDown = false;
		}
		return gestureDetector.onTouchEvent(event);
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
		loop.running = false;
	}
	
	public void setGravity(float ax, float ay) {
		if (loop.running) loop.setGravity(ax, ay);
	}
}
