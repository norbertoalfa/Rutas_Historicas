package com.example.rutashistoricas.RealidadAumentada;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import com.example.rutashistoricas.InterfazPrincipal.InfoPuntoInteres;
import com.example.rutashistoricas.Navegacion.RutaHistorica;
import com.example.rutashistoricas.R;

public class RealidadAumentada extends AppCompatActivity implements SensorEventListener {
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] gravityReading = new float[3];
    private  float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private SensorManager sensorManager;
    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId1;
    private int mActivePointerId2;


    private GLSurfaceView glView;
    private MyGLRenderer myGLRenderer;
    private final float[] posTriangulo = {0.43f, -0.9f, 0.0f};


    private static float lastTime = 0.0f;
    private static float currentTime = 0.0f;
    private static boolean inTime = false;
    private boolean pto_encontrado = false;

    private Intent intent_pto_interes = null;
    private int indexPuntoInteres = -1;

    RutaHistorica ruta = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id_titulo=0;

        ruta = (RutaHistorica) getIntent().getSerializableExtra("rutaHistorica");

        indexPuntoInteres = getIntent().getIntExtra("indexPuntoInteres", -1);


        switch (indexPuntoInteres) {
            case 1:

                id_titulo = R.string.nombre_pto_interes_1;

                setContentView(R.layout.activity_realidad_aumentada);
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                glView = new GLSurfaceView(this);
                myGLRenderer = new MyGLRenderer(this);
                glView.setRenderer(myGLRenderer);
                setContentView(glView);
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
                break;
            default:
                intent_pto_interes = new Intent(this, InfoPuntoInteres.class);
                intent_pto_interes.putExtra("indexPuntoInteres", indexPuntoInteres);
                startActivity(intent_pto_interes);
                break;
        }

        setTitle(getString(id_titulo));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(RealidadAumentada.this);
        builder1.setMessage("Busque las zonas de interés marcadas. Si desea obtener más información sobre alguna, apunte al símbolo de información durante 2 segundos");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.create().show();


    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
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
        pto_encontrado = false;
        setContentView(R.layout.activity_realidad_aumentada);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glView = new GLSurfaceView(this);
        myGLRenderer = new MyGLRenderer(this);
        glView.setRenderer(myGLRenderer);
        setContentView(glView);

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
        inTime = false;
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

        myGLRenderer.asignarDatosSensor(orientationAngles, new float[]{-rotationMatrix[2], -rotationMatrix[5], -rotationMatrix[8]});

        float distancia = (float) Math.pow(Math.pow(posTriangulo[0]+rotationMatrix[2],2)
                + Math.pow(posTriangulo[1]+rotationMatrix[5],2)
                + Math.pow(posTriangulo[0]+rotationMatrix[2],2) ,0.5);

        if (distancia<0.1) {
            if (!inTime){
                lastTime = SystemClock.uptimeMillis();
                inTime = true;
            }
            currentTime = SystemClock.uptimeMillis();
            if (currentTime - lastTime > 2000.0f) {
                if (!pto_encontrado) {
                    pto_encontrado = true;
                    Intent intent = new Intent(this, InfoPuntoInteres.class);
                    intent.putExtra("indexPuntoInteres", indexPuntoInteres);
                    startActivityForResult(intent, 111);
                    //finish();
                }
            }
        } else {
            inTime = false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(111);
        if (resultCode==111)
            finish();
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

}