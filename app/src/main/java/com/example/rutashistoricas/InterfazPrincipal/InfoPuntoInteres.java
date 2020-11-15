package com.example.rutashistoricas.InterfazPrincipal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rutashistoricas.Navegacion.Mapa;
import com.example.rutashistoricas.Navegacion.Navegador;
import com.example.rutashistoricas.R;
import com.example.rutashistoricas.RealidadAumentada.RealidadAumentada;


/**
 * Clase correspondiente a la actividad que muestra información sobre un punto de interés de una ruta concreta.
 *
 */
public class InfoPuntoInteres extends AppCompatActivity {
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
     * ID del punto de interés del cuál se va a mostrar información.
     */
    private int indexPuntoInteres = -1;


    /**
     * Se ejecuta al crear la actividad. Obtiene el ID del punto de interés, que debe ser enviado a esta actividad mediante un extra antes de iniciarla.
     * Inicializa el campo de texto del layout, el título de la actividad y selecciona la imágen que se mostrará en función del id del punto de interés en
     * el que estamos.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_punto_interes);

        int id_img=0, id_texto=0, id_titulo=0;

        indexPuntoInteres = getIntent().getIntExtra("indexPuntoInteres", -1);

        switch (indexPuntoInteres) {
            case 1:
                id_img = R.drawable.casa_federico;
                id_titulo = R.string.nombre_pto_interes_1;
                id_texto = R.string.pto_interes_1;
                break;
            default:
                finishActivity(0);
                break;

        }
        ImageView imgView = findViewById(R.id.imgPtoInteres);
        imgView.setImageResource(id_img);

        TextView textView = findViewById(R.id.textoPtoInteres);
        textView.setText(getString(id_texto));

        setTitle(getString(id_titulo));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * Método lanzado cuando pulsamos en la flecha de ir hacia atrás. Finalizamos esta actividad.
     *
     * @param item Item del menú que ha sido pulsado. En nuestro caso solo puede ser la flecha de ir hacia atrás.
     * @return Se devuelve siempre true.
     */
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
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
     * Método lanzado al pulsar el botón correspondiente a continuar la ruta. Se finaliza esta actividad y se vuelve a la
     * actividad {@link com.example.rutashistoricas.Navegacion.Navegador}.
     *
     * @param view Vista del botón.
     */
    public void continuarRuta(View view){
        setResult(111);
        finish();
    }

    /*
    @Override
    public void finish(){
        Intent intent = new Intent(InfoPuntoInteres.this, RealidadAumentada.class);
        startActivity(intent);
        super.finish();
    }

     */

    /**
     * Método lanzado al pulsar el botón correspondiente a obtener más información. Se lanzará una URL en el navegador.
     *
     * @param view Vista del botón.
     */
    public void masInfo(View view){
        String url = "";

        switch (indexPuntoInteres) {
            case 1:
                url = getString(R.string.url_casa_federico);
                break;
            default:
                finishActivity(0);
                break;

        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}