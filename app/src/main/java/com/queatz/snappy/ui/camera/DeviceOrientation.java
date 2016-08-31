package com.queatz.snappy.ui.camera;

/**
 * Created by jacob on 8/29/16.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.util.Log;

import com.queatz.snappy.shared.Config;

import static com.queatz.snappy.R.color.event;

/**
 * Created by abdelhady on 9/23/14.
 *
 * to use this class do the following 3 steps in your activity:
 *
 * define 3 sensors as member variables
 Sensor accelerometer;
 Sensor magnetometer;
 Sensor vectorSensor;
 DeviceOrientation deviceOrientation;
 *
 * add this to the activity's onCreate
 mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 deviceOrientation = new DeviceOrientation();
 *
 * add this to onResume
 mSensorManager.registerListener(deviceOrientation.getEventListener(), accelerometer, SensorManager.SENSOR_DELAY_UI);
 mSensorManager.registerListener(deviceOrientation.getEventListener(), magnetometer, SensorManager.SENSOR_DELAY_UI);
 *
 * add this to onPause
 mSensorManager.unregisterListener(deviceOrientation.getEventListener());
 *
 *
 * then, you can simply call * deviceOrientation.getOrientation() * wherever you want
 *
 *
 * another alternative to this class's approach:
 * http://stackoverflow.com/questions/11175599/how-to-measure-the-tilt-of-the-phone-in-xy-plane-using-accelerometer-in-android/15149421#15149421
 *
 */
public class DeviceOrientation {
    private int orientation = 0;

    private float[] mRotationMatrixFromVector = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] orientationVals = new float[3];

    public DeviceOrientation() {
    }

    public SensorEventListener getEventListener() {
        return sensorEventListener;
    }

    public int getOrientation() {
        return orientation;
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            orientation = calculateOrientation(event.values);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    private int calculateOrientation(float[] values) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, values);
        SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, orientationVals);

        // Optionally convert the result from radians to degrees
        orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
        orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
        orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);

        float roll = orientationVals[2];

        if (roll < 0) {
            roll = 360 + roll;
        }

        return ((Math.round(roll / 90)) * 90);
    }
}