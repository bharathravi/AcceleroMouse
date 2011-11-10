/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.skeletonapp;

import android.app.Activity;
import android.view.View.OnTouchListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class AcceleroMouse extends Activity {
    
    static final private int BACK_ID = Menu.FIRST;
    static final private int CLEAR_ID = Menu.FIRST + 1;

    private Button leftClick;
    private TextView text;
    private WakeLock mWakeLock;
    private MouseSimulator mouseSimulator;
    private NetworkProtocol net;
    
    public AcceleroMouse() {
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        net = new NetworkProtocol("10.0.0.7", 50154);
        net.init();
        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display mDisplay = mWindowManager.getDefaultDisplay();
        mouseSimulator = new MouseSimulator(this,
        		new Mouse(0,0, net.xMeters, net.yMeters, mDisplay, net));
        
        
       // LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout);    
        //linearLayout.addView(mouseSimulator);
       // leftClick = (Button)this.findViewById(R.id.click);
       // text = (TextView)this.findViewById(R.id.textview);       
       // leftClick.setOnClickListener(clicker);
        
        //Create a bright wake lock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
        
        setContentView(mouseSimulator);                 
    }
        
    
    OnClickListener clicker = new OnClickListener() {		
		@Override
		public void onClick(View view) {			
			TextView text = (TextView) findViewById(R.id.textview);
			text.setText("Bye");
		}
	};
	
	class MouseSimulator extends View implements SensorEventListener, OnTouchListener {

		private SensorManager mSensorManager;
		private Sensor mAccelerometer;
		private Mouse mouse;
		private float[] gravity = new float[3];
		private float kFilteringFactor = 0.6f;
		private float[] accel = new float[3];
		private long sensorTimeStamp;
		private long cpuTimeStamp;
		

		public MouseSimulator(Context context, Mouse mouse) {
			super(context);
			this.mouse = mouse;
			this.setOnTouchListener(touchListen);
			
			// Get an instance of the SensorManager
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mAccelerometer = mSensorManager
			.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);			
		}
		
		private OnTouchListener touchListen = new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent event) {
				switch(event.getAction()) {
				  case MotionEvent.ACTION_DOWN: mouse.click = 1; break;
				  case MotionEvent.ACTION_CANCEL:
				  case MotionEvent.ACTION_POINTER_UP:
				  case MotionEvent.ACTION_UP: mouse.click = 0; break;
				}
				return true;
			}
		};
		
		public void startSimulation() {
			/*
			 * It is not necessary to get accelerometer events at a very high
			 * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
			 * automatic low-pass filter, which "extracts" the gravity component
			 * of the acceleration. As an added benefit, we use less power and
			 * CPU resources.
			 */
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		}

		public void stopSimulation() {
		  mSensorManager.unregisterListener(this);
		}
		
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			final long now = sensorTimeStamp
			+ (System.nanoTime() - cpuTimeStamp);
		  mouse.update(accel[0], accel[1], System.nanoTime());
		  
		  // Convert X and Y into appropriate coordinates and send
		  net.sendUpdate(
				  (int)(mouse.X * net.xRes/net.xMeters), 
				  (int)(mouse.Y * net.yRes/net.yMeters), 
				  mouse.click);
		  invalidate();
		}
		
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	      // Need to override this to prevent onCreate from being called
		  // everytime size changes.
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;
			
			long now = event.timestamp;
			float alpha = 0.9f;

			accel[0] = event.values[0] * kFilteringFactor;
			accel[1] = event.values[1] * kFilteringFactor;
			accel[2] = event.values[2] * kFilteringFactor;			

	      //  accel[0] = alpha * accel[0] + (1 - alpha) * gravity[0];
	      //  accel[1] = alpha * accel[1] + (1 - alpha) * gravity[1];
	      //  accel[2] = alpha * accel[2] + (1 - alpha) * gravity[2];

			sensorTimeStamp = event.timestamp;
			cpuTimeStamp = System.nanoTime();
		}

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return false;
		}

	}
	

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
        mouseSimulator.startSimulation();
    }
    
    
    @Override
	protected void onPause() {
		super.onPause();
		/*
		 * When the activity is paused, we make sure to stop the simulation,
		 * release our sensor resources and wake locks
		 */
		// Stop the simulation
		mouseSimulator.stopSimulation();
		// and release our wake-lock
		mWakeLock.release();
	}

    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        menu.add(0, BACK_ID, 0, R.string.back).setShortcut('0', 'b');
        menu.add(0, CLEAR_ID, 0, R.string.clear).setShortcut('1', 'c');

        return true;
    }

    /**
     * Called right before your activity's option menu is displayed.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Before showing the menu, we need to decide whether the clear
        // item is enabled depending on whether there is text to clear.
      

        return true;
    }

    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case BACK_ID:
            finish();
            return true;
        case CLEAR_ID:
       
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A call-back for when the user presses the back button.
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * A call-back for when the user presses the clear button.
     */
    OnClickListener mClearListener = new OnClickListener() {
        public void onClick(View v) {
          
        }
    };
}
