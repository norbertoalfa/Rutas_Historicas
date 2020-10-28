package com.example.rutashistoricas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
}