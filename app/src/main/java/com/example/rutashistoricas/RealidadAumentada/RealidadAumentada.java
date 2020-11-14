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

/**
 * Clase correspondiente a la actividad de visualización de la foto 360 para el punto de interés actual.
 */
public class RealidadAumentada extends AppCompatActivity implements SensorEventListener {
    /**
     * Datos de lectura del acelerómetro.
     */
    private final float[] accelerometerReading = new float[3];
    /**
     * Datos de lectura del magnetómetro.
     */
    private final float[] magnetometerReading = new float[3];
    /**
     * Matriz de orientación obtenida mediante las lecturas de los sensores.
     */
    private  float[] rotationMatrix = new float[9];
    /**
     * Ángulos de orientación del dispositivo calculados a partir de la matriz de orientación.
     */
    private final float[] orientationAngles = new float[3];

    /**
     * Objeto utilizado para manejar los sensores con los que se van a trabajar.
     */
    private SensorManager sensorManager;
    /**
     * Objeto requerido para la detección de gestos en la pantalla táctil.
     */
    private VelocityTracker mVelocityTracker = null;
    /**
     * Índice del puntero 1 del evento táctil (multitouch).
     */
    private int mActivePointerId1;
    /**
     * Índice del puntero 2 del evento táctil (multitouch).
     */
    private int mActivePointerId2;

    /**
     * Visualizador de escenas 3D mediante el uso de OpenGL
     */
    private GLSurfaceView glView;
    /**
     * Objeto que representa el renderizado de la escena 3D para simular la visualización de una foto 360.
     */
    private MyGLRenderer myGLRenderer;
    /**
     * Posición (en 3D) del punto de interés etiquetado (casa de Federico García Lorca).
     */
    private final float[] posCasa = {0.43f, -0.9f, 0.0f};

    /**
     * Tiempo en el que comenzó a visualizar el elemento etiquetado (milisegundos).
     */
    private static float lastTime = 0.0f;
    /**
     * Tiempo actual (milisegundos).
     */
    private static float currentTime = 0.0f;
    /**
     *  Variable que controla si se está visualizando el elemento etiquetado y el tiempo inicial
     * ya se ha anotado.
     */
    private static boolean inTime = false;
    /**
     * Variable que indica si se ha lanzado ya la actividad "InfoPuntoInteres".
     */
    private boolean pto_encontrado = false;
    /**
     * Objeto "Intent" utilizado para crear la actividad "InfoPuntoInteres".
     */
    private Intent intent_pto_interes = null;
    /**
     * Índice del punto de interés que se está visualizando actualmente.
     */
    private int indexPuntoInteres = -1;
    /**
     * Ruta histórica que se está realizando actualmente (cuando se crea la actividad RealidadAumentada)
     */
    RutaHistorica ruta = null;

    /**
     *  Se ejecuta al iniciar la actividad. Visualiza la escena 3D y muestra un cuadro de diálogo
     * indicando qué gestos debe realizar para poder obtener más información sobre el edificio
     * etiquetado.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
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

    /**
     *  Se ejecuta cuando un "item" del menú de opciones es seleccionado. El ítem seleccionado en
     * nuestro caso siempre es volver hacia atrás, por lo que finalizamos la actividad.
     *
     * @param item  Ítem seleccionado en el menú de opciones.
     */
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    /**
     *  Se ejecuta al detectar un evento táctil. Detecta si se realiza el movimiento para retroceder,
     *  es decir, mover los 2 dedos de izquierda a derecha.
     *
     * @param event Evento táctil detectado.
     */
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

    /**
     *  Se ejecuta cuando la precisión de los sensores cambia. No realiza ninguna acción nueva.
     *
     * @param sensor Sensor que ha experimentado el cambio de precisión.
     * @param accuracy Nueva precisión.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     *  Se ejecuta al volver desde el estado de pausa de nuevo a la actividad. Visualiza la escena 3D
     * de nuevo (como en onCreate pero sin el cuadro de diálogo que aporta la información inicial)
     */
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

    /**
     *  Se ejecuta al poner en pausa la actividad (ya sea por estar la aplicación en segundo plano
     * o por haber llamado a otra actividad). Realiza lo mismo que la clase de la que hereda y además
     * indica que ya no se está tomando información acerca de los sensores ni si el dispositivo
     * apunta hacia la zona etiquetada (para evitar errores al volver de nuevo a la actividad)
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
        inTime = false;
    }

    /**
     *  Se ejecuta cuando un sensor cualquiera detecta nuevos valores. Se actualizan las lecturas
     * de los sensores utilizados (el resto se ignoran). Por consiguiente se actualizan la matriz de
     * rotación y los ángulos. Además se comprueba si se está mirando en la dirección del elemento
     * etiquetado y durante cuanto tiempo se ha observado.
     *
     * @param event Eventos del sensor que ha detectado cambios.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        updateOrientationAngles();

        myGLRenderer.asignarDatosSensor(orientationAngles, new float[]{-rotationMatrix[2], -rotationMatrix[5], -rotationMatrix[8]});

        float distancia = (float) Math.pow(Math.pow(posCasa[0]+rotationMatrix[2],2)
                + Math.pow(posCasa[1]+rotationMatrix[5],2)
                + Math.pow(posCasa[0]+rotationMatrix[2],2) ,0.5);

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

    /**
     *  Se ejecuta cuando una actividad que se ha creado anteriormente, finaliza devolviendo un
     * resultado. Si el resultado corresponde con "111" quiere decir que quiere salir directamente
     * a la navegación (continuar la ruta), por lo que finaliza ésta actividad inmediatamente.
     *
     * @param resultCode Código devuelto por la actividad finalizada.
     * @param requestCode Utilizado para llamar al mismo método de la clase padre.
     * @param data Utilizado para llamar al mismo método de la clase padre.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(111);
        if (resultCode==111)
            finish();
    }

    /**
     *  Se ejecuta cuando justo después del método onSensorChange, para actualizar la matriz de rotación
     * y los ángulos en función de las nuevas lecturas de los sensores.
     */
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

}