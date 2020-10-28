package com.example.rutashistoricas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.rutashistoricas.R.id.boton_federico;

public class MainActivity extends AppCompatActivity {
    public static final String ID_PERSONAJE = "com.example.myfirstapp.ID_PERSONAJE";
    private String textoBienvenida = "Bienvenido a la aplicacion...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void irPantallaPersonaje(View view) {
        Intent intent = new Intent(this, PantallaPersonaje.class);
        Bundle b = new Bundle();
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
        intent.putExtras(b);
        startActivity(intent);
    }
}