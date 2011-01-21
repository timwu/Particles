package com.timwu.Particles.math;

public class Vector2d {
	private float x, y;
	
	public Vector2d(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2d(Vector2d v) {
		x = v.getX();
		y = v.getY();
	}
	
	public void multiplyAdd(float a, Vector2d v) {
		x += a * v.getX();
		y += a * v.getY();
	}
	
	public void multiply(float a, float b) {
		x *= a;
		y *= b;
	}
	
	public float dot(Vector2d right) {
		return x * right.getX() + y * right.getY();
	}
	
	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	@Override
	public String toString() {
		return String.format("(%1$, %2$)", x, y);
	}
}
