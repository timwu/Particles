package com.timwu.Particles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.timwu.Particles.math.Particle;
import com.timwu.Particles.math.Segment;
import com.timwu.Particles.math.Physics;
import com.timwu.Particles.math.Vector2d;

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
		private List<Particle> particles = new ArrayList<Particle>();
		private List<Segment> segments = new ArrayList<Segment>();
		private Segment currentLine;
		private long prevTick;
		private float curTimeslice;
		private float avgTimeslice;
		private Vector2d gravity = new Vector2d(0.0f, Physics.G_METERS);
		private Vector2d sprayerPos;
		private boolean touchDown;
		private MotionEvent downEvent;
		private MotionEvent scrollEvent;
		private boolean running = false;
		private Random r = new Random();
		
		private void init() {
			Log.i(TAG, "Starting particle simulator.");
			prevTick = System.nanoTime();
			Log.i(TAG, "Starting with tick " + prevTick);
			
			// Tick once to initialize the avg timeslice;
			tick();
			avgTimeslice = curTimeslice;
			
			// Place the sprayer
			sprayerPos = new Vector2d(getWidth() / 2, getHeight() * 0.2f);
			
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
						
			// Apply accelerations and move
			for (Particle p : particles) {
				p.accelerate(curTimeslice, gravity);
				for (Segment segment : segments) {
					p.bounceOff(curTimeslice, segment);
				}
				p.move(curTimeslice, gravity);
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
				c.drawText(getFps(), 0, 10, paint);
			}
			getHolder().unlockCanvasAndPost(c);
		}
		
		private void tick() {
			long curTick = System.nanoTime();
			curTimeslice = (curTick - prevTick) / Physics.NANO_SECONDS_PER_SECOND;
			avgTimeslice = (curTimeslice * 19 + avgTimeslice) / 20;
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
			gravity.set(xdpi * Physics.G_INCHES / 100.0f * (ax / Physics.G_METERS),
					    ydpi * Physics.G_INCHES / 100.0f * (ay / Physics.G_METERS));
		}
		
		private static final int FPS_DISPLAY_FREQUENCY = 10;
		private int fpsPrintDivider = 0;
		private String savedFps;
		private String getFps() {
			if (fpsPrintDivider % FPS_DISPLAY_FREQUENCY == 0) {
				float fps = 1 / avgTimeslice;
				savedFps = String.format("%1$.2f fps", fps);
			}
			fpsPrintDivider = (fpsPrintDivider + 1) % FPS_DISPLAY_FREQUENCY;
			return savedFps;
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
		loop.setGravity(ax, ay);
	}
}
