package com.example.rutashistoricas;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

public class ListadoRutas extends AppCompatActivity {
    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId1;
    private int mActivePointerId2;
    int index_pnj = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_rutas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        String name = "", titulo_ruta_1 = "", texto_ruta_1 = "", texto_ruta_2 = "", texto_ruta_3 = "";
        if (b != null) {
            index_pnj = b.getInt("index_pnj");
            switch (index_pnj) {
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
        textView = findViewById(R.id.tituloRuta2);
        textView.setText(texto_ruta_2);
        textView = findViewById(R.id.tituloRuta3);
        textView.setText(texto_ruta_3);

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
        b.putInt("index_pnj", index_pnj);
        b.putInt("index_ruta", 1);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void iniciarRuta2(View view) {
        Bundle b = new Bundle();
        b.putInt("index_pnj", index_pnj);
        b.putInt("index_ruta", 2);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
    }
    public void iniciarRuta3(View view) {
        Bundle b = new Bundle();
        b.putInt("index_pnj", index_pnj);
        b.putInt("index_ruta", 3);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
    }


}
