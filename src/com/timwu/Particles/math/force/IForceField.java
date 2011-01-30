package com.timwu.Particles.math.force;

import com.timwu.Particles.math.Vector2d;


public interface IForceField {
	public Vector2d getForce(Vector2d p);
}
