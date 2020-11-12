package com.example.rutashistoricas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;
import androidx.core.view.MotionEventCompat;

public class RealidadAumentada extends Activity implements SensorEventListener {
    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId1;
    private int mActivePointerId2;
    private SensorManager sensorManager;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] gravityReading = new float[3];
    private  float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private final float[] posTriangulo = {0.0f, 1.0f, 0.0f};

    private GLSurfaceView glView;
    private MyGLRenderer myGLRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realidad_aumentada);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glView = new GLSurfaceView(this);
        myGLRenderer = new MyGLRenderer();
        glView.setRenderer(myGLRenderer);
        setContentView(glView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        float xVel1 = 0.0f;
        float yVel1 = 0.0f;
        float xVel2 = 0.0f;
        float yVel2 = 0.0f;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }

        if (action == MotionEvent.ACTION_MOVE && event.getPointerCount() == 2) {
            mActivePointerId1 = event.getPointerId(0);
            mActivePointerId2 = event.getPointerId(1);

            mVelocityTracker.addMovement(event);
            mVelocityTracker.computeCurrentVelocity(1000);

            xVel1 = mVelocityTracker.getXVelocity(mActivePointerId1);
            yVel1 = mVelocityTracker.getYVelocity(mActivePointerId1);

            xVel2 = mVelocityTracker.getXVelocity(mActivePointerId2);
            yVel2 = mVelocityTracker.getYVelocity(mActivePointerId2);

            if (Math.abs(yVel1)<1000 && xVel1>100 && Math.abs(yVel2)<1000 && xVel2>100) {
                finish();
            }

            float distancia = (float) Math.pow(Math.pow(posTriangulo[0]+rotationMatrix[2],2)
                    + Math.pow(posTriangulo[1]+rotationMatrix[5],2)
                    + Math.pow(posTriangulo[0]+rotationMatrix[2],2) ,0.5);

            if (Math.abs(yVel1)<1000 && xVel1<-100 && Math.abs(yVel2)<1000 && xVel2<-100 && distancia<0.05) {
                Intent intent = new Intent(this, InfoPuntoInteres.class);
                startActivity(intent);
            }
        }

        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
        finish();
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            System.arraycopy(event.values, 0, gravityReading,
                    0, gravityReading.length);
        }

        updateOrientationAngles();

        // "mOrientationAngles" now has up-to-date information.
        /*String str_valores = Float.toString(orientationAngles[0]) +
                "\n" + Float.toString(orientationAngles[1]) +
                "\n" + Float.toString(orientationAngles[2]);
        TextView textView = findViewById(R.id.valores);
        textView.setText(str_valores);*/
        myGLRenderer.asignarDatosSensor(orientationAngles, new float[]{-rotationMatrix[2], -rotationMatrix[5], -rotationMatrix[8]});

        float distancia = (float) Math.pow(Math.pow(posTriangulo[0]+rotationMatrix[2],2)
                + Math.pow(posTriangulo[1]+rotationMatrix[5],2)
                + Math.pow(posTriangulo[0]+rotationMatrix[2],2) ,0.5);

        if (distancia<0.05) {
            Intent intent = new Intent(this, InfoPuntoInteres.class);
            startActivity(intent);
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        //SensorManager.getRotationMatrix(rotationMatrix, null,
        //        gravityReading, magnetometerReading);
        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

}