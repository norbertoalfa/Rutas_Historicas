package com.example.rutashistoricas.InterfazPrincipal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import com.example.rutashistoricas.Navegacion.Mapa;
import com.example.rutashistoricas.R;
import com.example.rutashistoricas.RealidadAumentada.RealidadAumentada;

public class ListadoRutas extends AppCompatActivity {
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
     * Nombre de la actividad, que será el nombre del personaje para el cuál se están visualizando las rutas.
     */
    private static String name = "";

    /**
     * Nombre de la ruta con ID=1 del personaje.
     */
    private static String titulo_ruta_1 = "";
    //private static String texto_ruta_1 = "";
    /**
     * Nombre de la ruta con ID=2 del personaje.
     */
    private static String texto_ruta_2 = "";
    /**
     * Nombre de la ruta con ID=3 del personaje.
     */
    private static String texto_ruta_3 = "";

    /**
     * ID del personaje para el cual se están visualizando las rutas.
     */
    private static int idPnj = 0;

    /**
     * Cuadro de diálogo que se está mostrando en pantalla actualmente, en caso de haberlo.
     */
    AlertDialog currentDialog = null;

    /**
     * Se ejecuta al crear la actividad. Obtiene el ID del personaje seleccionado, que es enviado por la actividad {@link PantallaPersonaje}
     * (actividad padre de esta).
     * Inicializa los campos de texto del layout con el nombre de las rutas y el nombre del personaje asociados a dicho ID.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_rutas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("idPnj");
            switch (idPnj) {
                case 1:
                    name = getString(R.string.nombre_federico);
                    titulo_ruta_1 = getString(R.string.federico_titulo_ruta_1);
                    texto_ruta_2 = getString(R.string.federico_titulo_ruta_2);
                    texto_ruta_3 = getString(R.string.federico_titulo_ruta_3);
            }
        }

        setTitle(name);
        TextView textView;

        textView = findViewById(R.id.tituloRuta1);
        textView.setText(titulo_ruta_1);
        textView.setAllCaps(true);
        textView = findViewById(R.id.tituloRuta2);
        textView.setText(texto_ruta_2);
        textView.setAllCaps(true);
        textView = findViewById(R.id.tituloRuta3);
        textView.setText(texto_ruta_3);
        textView.setAllCaps(true);

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
     * Método ejecutado al pulsar el botón correspondiente a iniciar la primera ruta.
     * Lanza la actividad {@link com.example.rutashistoricas.Navegacion.Mapa} y le envía el ID del personaje
     * y el ID de la ruta, en este caso 1.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void iniciarRuta1(View view) {
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 1);
        Intent intent = new Intent(this, Mapa.class);
        //Intent intent = new Intent(this, RealidadAumentada.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a iniciar la segunda ruta.
     * Esta ruta no está implementada e informa de ello por pantalla.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void iniciarRuta2(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ListadoRutas.this);
        builder.setMessage(getString(R.string.func_no_prog));
        builder.setCancelable(true);
        currentDialog = builder.create();
        currentDialog.show();
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 2);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a iniciar la primera ruta.
     * Esta ruta no está implementada y por tanto esta función no hace nada.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void iniciarRuta3(View view) {


        // No borrar este comentario
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 3);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

}
