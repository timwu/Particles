package com.timwu.Particles.math.force;

import com.timwu.Particles.math.Vector2d;

public class SimpleForce implements IForceField {
	private float xScale, yScale;
	private Vector2d direction = new Vector2d(0.0f, 0.0f);
	private Vector2d force = new Vector2d(0.0f, 0.0f);
	
	public void setDirection(float x, float y) {
		direction.set(x, y);
		updateForce();
	}
	
	private void updateForce() {
		force.set(direction);
		force.multiply(xScale, yScale);
	}
	
	public void setScale(float xScale, float yScale) {
		this.xScale = xScale;
		this.yScale = yScale;
		updateForce();
	}

	@Override
	public Vector2d getForce(Vector2d p) {
		return new Vector2d(direction.getX() * xScale, direction.getY() * yScale);
	}
}
