package com.example.rutashistoricas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.TextView;

public class PantallaPersonaje extends AppCompatActivity {
    private VelocityTracker mVelocityTracker = null;
    private int mActivePointerId1;
    private int mActivePointerId2;
    private int index_pnj = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_personaje);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        String  name = "",
                description = "";

        if (b != null) {
            index_pnj = b.getInt("index_pnj");
            switch (index_pnj) {
                case 1:
                    name = getString(R.string.nombre_federico);
                    description = getString(R.string.descripcion_federico);
            }

        }

        setTitle(name);
        TextView textView = findViewById(R.id.descripcion);
        textView.setText(description);
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

            if (Math.abs(yVel1)<1000 && xVel1>100 && Math.abs(yVel2)<1000 && xVel2>100)
                finish();
        }

        return true;
    }

    public void saberMas(View view) {
        //Intent intent = new Intent(this, PantallaPersonaje.class);
        //startActivity(intent);
    }

    public void mostrarRutas(View view) {

        Bundle b = new Bundle();
        b.putInt("index_pnj", index_pnj);

        Intent intent = new Intent(this, ListadoRutas.class);
        intent.putExtras(b);
        startActivity(intent);
    }
}