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

public class PantallaPersonaje extends AppCompatActivity {
    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId1;
    private int mActivePointerId2;
    private static int idPnj = 0;
    private static String nombre = "",
                   nacimiento = "",
                   fallecimiento = "",
                   categorias = "",
                   descripcion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_personaje);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("index_pnj");
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

    /*@Override
    protected void onResume() {
        super.onResume();

        switch (index_pnj) {
            case 1:
                nombre = getString(R.string.nombre_federico);
                nacimiento = getString(R.string.nacimiento_federico);
                fallecimiento = getString(R.string.fallecimiento_federico);
                categorias = getString(R.string.categorias_federico);
                descripcion = getString(R.string.descripcion_federico);
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
    }*/

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

    public void saberMas(View view) {
        Intent intent = new Intent(this, SaberMas.class);
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void mostrarRutas(View view) {

        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);

        Intent intent = new Intent(this, ListadoRutas.class);
        intent.putExtras(b);
        startActivity(intent);
    }
}