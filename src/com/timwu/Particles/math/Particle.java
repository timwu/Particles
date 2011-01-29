package com.timwu.Particles.math;

import android.graphics.Color;

public class Particle {
	private Vector2d pos, v;
	private float r;
	private int color;
	private float bounce = 0.5f;
	
	public Particle(Vector2d pos, Vector2d v, float radius, int color) {
		this.pos = new Vector2d(pos);
		this.v  = new Vector2d(v);
		this.r = radius;
		this.color = color;
	}
	
	public void move(float dt, Vector2d a) {
		// Just doing a naive implementation where velocity will be
		// constant over the timeslice. Probably not too bad an approximation.
		pos.multiplyAdd(dt, v);
	}
	
	public void accelerate(float dt, Vector2d a) {
		v.multiplyAdd(dt, a);
	}
	
	public void bounceOff(float dt, Segment s) {
		float d = s.distanceToPoint(pos);
		if (d <= Math.abs(dt * v.dot(s.getN())) + Physics.FUDGE ) {
			color = Color.MAGENTA;
			v.reflect(s.getN());
			v.scale(bounce);
		}
	}
	
	public Vector2d getPos() {
		return pos;
	}
	
	public Vector2d getV() {
		return v;
	}
	
	public float getR() {
		return r;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int c) {
		color = c;
	}

	public String toString() {
		return "Particle @ " + pos + " going " + v;
	}
}
