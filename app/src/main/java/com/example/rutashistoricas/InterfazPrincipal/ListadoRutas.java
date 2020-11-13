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
    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId1;
    private int mActivePointerId2;
    private static String name = "";
    private static int idPnj = 0;

    AlertDialog currentDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_rutas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        String name = "", titulo_ruta_1 = "", texto_ruta_1 = "", texto_ruta_2 = "", texto_ruta_3 = "";
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

    // para volver atrás con los dos dedos
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

    public void iniciarRuta1(View view) {
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 1);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
    }

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
