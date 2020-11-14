package com.example.rutashistoricas.InterfazPrincipal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rutashistoricas.R;

public class SaberMas extends AppCompatActivity {
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
     * Nombre de la actividad, que será el nombre del personaje cuya biografía se está mostrando.
     */
    private static String nombre = "";

    /**
     * Biografía del personaje.
     */
    private static String biografia = "";


    /**
     * Se ejecuta al crear la actividad. Obtiene el ID del personaje seleccionado, que es enviado por la actividad {@link PantallaPersonaje}
     * (actividad padre de esta).
     * Inicializa los campos de texto del layout con el nombre del personaje y la biografía asociados a dicho ID.
     * A continuación, asocia una url con más información del personaje al botón de maś información, de forma que cuando se hace click en él, se abre la url en un navegador.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saber_mas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("idPnj");
            switch (idPnj) {
                case 1:
                    nombre = getString(R.string.nombre_federico);
                    biografia = getString(R.string.biografia_federico);
            }
        }

        setTitle(nombre);
        TextView textView = findViewById(R.id.biografia);
        textView.setText(biografia);


        Button botonLink = findViewById(R.id.botonLink);

        String url = getString(R.string.url_federico);

        botonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
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
}