package com.example.rutashistoricas.InterfazPrincipal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.TextView;

import com.example.rutashistoricas.InterfazPrincipal.ListadoRutas;
import com.example.rutashistoricas.R;


/**
 * Clase correspondiente a la actividad que nos presenta un personaje concreto.
 * Se nos presenta una breve información del personaje y nos permite acceder a otra pantalla
 * para saber más acerca del mismo o al listado de sus rutas.
 *
 */
public class PantallaPersonaje extends AppCompatActivity {
    /**
     * Para gestión interna de los eventos táctiles. Nos permite saber a que velocidad se mueven los punteros por la pantalla durante un evento táctil.
     */
    private VelocityTracker mVelocityTracker = null;

    /**
     * Para gestión interna de los eventos táctiles. ID correspondiente a un puntero que produce un evento táctil.
     */
    private int mActivePointerId1;
    /**
     * Para gestión interna de los eventos táctiles. ID correspondiente a un puntero que produce un evento táctil.
     */
    private int mActivePointerId2;

    /**
     * ID del personaje para el cual se están visualizando las rutas.
     */
    private static int idPnj = 0;

    /**
     * Nombre de la actividad, que será el nombre del personaje seleccionado.
     */
    private static String nombre = "";

    /**
     * Fecha y lugar de nacimiento del personaje seleccionado.
     */
    private static String nacimiento = "";

    /**
     * Fecha y lugar de fallecimiento del personaje seleccionado.
     */
    private static String fallecimiento = "";

    /**
     * Categorías del personaje seleccionado: sus ocupaciones, movimientos a los que pertenecía...
     */
    private static String categorias = "";

    /**
     * Breve descripción del personaje seleccionado.
     */
    private static String descripcion = "";

    /**
     * Se ejecuta al crear la actividad. Obtiene el ID del personaje seleccionado, que es enviado por la actividad {@link com.example.rutashistoricas.InterfazPrincipal.MainActivity}
     * (actividad padre de esta).
     * Inicializa los campos de texto del layout con el nombre del personaje, su nacimiento, su fallecimiento, sus categorías y su descripción.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_personaje);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("idPnj");
            switch (idPnj) {
                case 1:
                    nombre = getString(R.string.nombre_federico);
                    nacimiento = "Nacimiento: " + getString(R.string.nacimiento_federico);
                    fallecimiento = "Fallecimiento: " + getString(R.string.fallecimiento_federico);
                    categorias = "Categorías: " + getString(R.string.categorias_federico);
                    descripcion = getString(R.string.descripcion_federico);
            }
        }

        setTitle(nombre);
        TextView textView = findViewById(R.id.nacimiento);
        textView.setText(nacimiento);
        textView = findViewById(R.id.fallecimiento);
        textView.setText(fallecimiento);
        textView = findViewById(R.id.categorias);
        textView.setText(categorias);
        textView = findViewById(R.id.descripcion);
        textView.setText(descripcion);
    }

    /**
     * Se ejecuta cuando se produce un evento táctil. Se encarga de comprobar si se hace un movimiento con dos dedos desplazándose
     * por la pantalla de izquierda a derecha, y en caso de producirse finaliza la actividad para volver a su actividad padre.
     *
     * @param event Evento táctil.
     *
     * @return Devuelve siempre true.
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

            if (Math.abs(yVel1)<1000 && xVel1>100 && Math.abs(yVel2)<1000 && xVel2>100)
                finish();
        }

        return true;
    }

    /**
     * Método ejecutado al pulsar el botón para saber más acerca del personaje.
     * Lanza la actividad {@link com.example.rutashistoricas.InterfazPrincipal.SaberMas} y le envía el ID del personaje.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void saberMas(View view) {
        Intent intent = new Intent(this, SaberMas.class);
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        intent.putExtras(b);
        startActivity(intent);
    }

    /**
     * Método ejecutado al pulsar el botón para mostrar las rutas del personaje.
     * Lanza la actividad {@link com.example.rutashistoricas.InterfazPrincipal.ListadoRutas} y le envía el ID del personaje.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void mostrarRutas(View view) {

        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);

        Intent intent = new Intent(this, ListadoRutas.class);
        intent.putExtras(b);
        startActivity(intent);
    }
}