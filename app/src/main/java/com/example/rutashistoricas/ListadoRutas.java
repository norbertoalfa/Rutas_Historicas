package com.example.rutashistoricas;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
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

        String name = "", texto_ruta_1 = "";
        if (b != null) {
            index_pnj = b.getInt("index_pnj");
            switch (index_pnj) {
                case 1:
                    name = getString(R.string.nombre_federico);
                    texto_ruta_1 = getString(R.string.texto_ruta_1);
            }
        }

        setTitle(name);
        TextView textView = findViewById(R.id.textoRuta1);
        textView.setText(texto_ruta_1);
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
        Intent intent = new Intent(this, Mapa.class);
        startActivity(intent);
    }

}
