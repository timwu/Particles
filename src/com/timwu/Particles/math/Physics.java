package com.timwu.Particles.math;

public class Physics {
	// Gravity in inches/s^2
	public static final float G_INCHES = 32.2f * 12;
	// Gravity in meters/s^2
	public static final float G_METERS = 9.8f;
	// PI in a float
	public static final float PI = (float) Math.PI;
	// The universal Fudge constant
	public static final float FUDGE = 0.001f;
	
	public static float pointDistanceToLine(Vector2d point, Segment segment) {
		Vector2d v = new Vector2d(segment.getStart());
		v.multiplyAdd(-1.0f, point);
		Vector2d u = new Vector2d(segment.getEnd());
		u.multiplyAdd(-1.0f, segment.getStart());
		float len2 = u.dot(u);
		float det = -1.0f * v.dot(u);
		if (det < 0  || det > len2) {
			u.multiplyAdd(-1.0f, point);
			return (float) Math.sqrt(Math.min(v.dot(v), u.dot(u)));
		}
		
		det = u.cross(v);
		return (float) Math.sqrt(det * det / len2);
	}
}
