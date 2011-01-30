package com.timwu.Particles.math.force;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.timwu.Particles.math.Vector2d;

public class SuckForce implements IForceField {

	private Vector2d pos;
	private float topAccel, duration, completion, maxDistance;
	private Interpolator interpolator;
	
	public SuckForce(float x, float y, float maxDistance, float topAccel, float duration) {
		this.pos = new Vector2d(x, y);
		this.topAccel = topAccel;
		this.duration = duration;
		this.maxDistance = maxDistance;
		this.interpolator = new DecelerateInterpolator();
		this.completion = 0;
	}
	
	@Override
	public Vector2d getForce(Vector2d p) {
		Vector2d force = new Vector2d(p).multiplyAdd(-1.0f, pos);
		if (force.length() > maxDistance) {
			force.set(0.0f, 0.0f);
			return force;
		}
		force.scale(-topAccel / maxDistance * interpolator.getInterpolation(completion / duration));
		return force;
	}

	@Override
	public void update(float dt) {
		completion += dt;
	}

	@Override
	public boolean getPurge() {
		return completion >= duration;
	}
	
	public Vector2d getPos() {
		return pos;
	}
}
