package com.example.android.skeletonapp;

import android.graphics.Bitmap;
import android.view.Display;
import android.view.Surface;

public class Mouse {
	public float mass = 1000;
	public float staticFriction = 0.1f;
	public float dynamicFriction = 1.3f;
	
	public float X,Y;
	private float accelX = 0, accelY = 0;
	private float xMax, yMax;
	private float lastT = 0;
	private float lastDeltaT = 0;
	private Display mDisplay;
	private NetworkProtocol net;
	public int click;
	private float lastX;
	private float lastY;

	Mouse(float x, float y, float xMax, float yMax, Display display, NetworkProtocol net) {
		X = x;
		Y = y;
		this.xMax = xMax;
		this.yMax = yMax;
		this.mDisplay = display;
		this.net = net;
	}

	public void update(float ax, float ay, long now) {
		float mSensorX = 0;
		float mSensorY = 0;				
		
		switch (mDisplay.getRotation()) {
		case Surface.ROTATION_0:
			mSensorX = ax;
			mSensorY = ay;
			break;
		case Surface.ROTATION_90:
			mSensorX = -ax;
			mSensorY = -ay;
			break;
		case Surface.ROTATION_180:
			mSensorX = -ax;
			mSensorY = -ay;
			break;
		case Surface.ROTATION_270:
			mSensorX = ax;
			mSensorY = ay;
			break;					
		}
				
		if (lastT != 0) {
			final float deltaT = (float) (now - lastT)
			                 * (1.0f / 1000000000.0f);					
			if (lastDeltaT != 0) {
		     performScience(mSensorX, mSensorY, deltaT, lastDeltaT);			
			}
			lastDeltaT = deltaT;
		}			    
		lastT = now;
	}

	private void performScience(float sx, float sy, 
			float deltaT, float lastDeltaT) {		
		// Force of gravity applied to our virtual object
		final float m = 10000000.0f; // mass of our virtual object
		final float gx = -sx * m ;
		final float gy = -sy * m;

		/*
		 * �F = mA <=> A = �F / m We could simplify the code by
		 * completely eliminating "m" (the mass) from all the equations,
		 * but it would hide the concepts from this sample code.
		 */
		final float invm = 1.0f / m;
		final float ax = gx * invm - staticFriction;
		final float ay = gy * invm - staticFriction;

		/*
		 * Time-corrected Verlet integration The position Verlet
		 * integrator is defined as x(t+�t) = x(t) + x(t) - x(t-�t) +
		 * a(t)�t�2 However, the above equation doesn't handle variable
		 * �t very well, a time-corrected version is needed: x(t+�t) =
		 * x(t) + (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2 We also add
		 * a simple friction term (f) to the equation: x(t+�t) = x(t) +
		 * (1-f) * (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2
		 */
		final float dTdT = deltaT * deltaT;
		float x = X + (1.0f - dynamicFriction) * (deltaT/lastDeltaT)
				* (X - lastX) + accelX * dTdT;
		float y = Y + (1.0f - dynamicFriction) * (deltaT/lastDeltaT)
				* (Y - lastY) + accelY * dTdT;
	
		if (x > xMax) x= xMax;
		if (x < 0) x = 0;
		
		if (y > yMax) y= yMax;
		if (y < 0) y = 0;
				
		lastX = X;
		lastY = Y;
		X = x;
		Y = y;
		accelX = ax;
		accelY = ay;
	}
}
