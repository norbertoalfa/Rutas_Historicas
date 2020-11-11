package com.example.rutashistoricas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PantallaPersonaje extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_personaje);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        String  name = "",
                description = "";

        if (b != null) {
            name = b.getString("nombre");
            description = b.getString("descripcion");
        }

        setTitle(name);
        TextView textView = findViewById(R.id.descripcion);
        textView.setText(description);
    }

    public void ver3d(View view) {
        //Intent intent = new Intent(this, PruebaCamara.class);
        Intent intent = new Intent(this, Prueba3d.class);
        /*Bundle b = new Bundle();
        String  name = "",
                description = "";
        switch (view.getId()) {
            case (R.id.boton_federico):
                name = getString(R.string.nombre_federico);
                description = getString(R.string.descripcion_federico);
                break;
        }

        b.putCharSequence("nombre", name);
        b.putCharSequence("descripcion", description);
        intent.putExtras(b);*/
        startActivity(intent);
    }
}