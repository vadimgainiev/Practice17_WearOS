package com.example.weorostesting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.weorostesting.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private static final String OUTPUT_TEMPLATE = "X:%.0f;\nY:%.0f;\nZ:%.0f";
    private static final int ROTATION_WAIT_TIME_MS = 100;

    public SensorManager sensorManager;
    public Sensor Gyroscope;
    public SensorEventListener sensorEventListener;
    public long previousMillis;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        previousMillis = 0;

        sensorEventListener = new SensorEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;

                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    long now = System.currentTimeMillis();

                    if (now - previousMillis > ROTATION_WAIT_TIME_MS) {
                        previousMillis = now;
                        float x, y, z;

                        float[] rotationMatrix = new float[16];
                        SensorManager.getRotationMatrixFromVector(
                                rotationMatrix, event.values);
                        float[] remappedRotationMatrix = new float[16];
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_X,
                                SensorManager.AXIS_Z,
                                remappedRotationMatrix
                        );

                        float[] orientations = new float[3];
                        SensorManager.getOrientation(remappedRotationMatrix, orientations);
                        for (int i = 0; i < 3; i++) {
                            orientations[i] = (float) (Math.toDegrees(orientations[i]));
                        }
                        x = orientations[0];
                        y = orientations[1];
                        z = orientations[2];

                        binding.coordinatesTv.setText(String.format(OUTPUT_TEMPLATE, x, y, z));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, Gyroscope,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }
}