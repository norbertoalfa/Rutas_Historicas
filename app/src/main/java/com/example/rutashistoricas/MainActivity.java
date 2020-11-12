package com.example.rutashistoricas;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.rutashistoricas.R.id.boton_federico;

public class MainActivity extends AppCompatActivity {
    public static final String ID_PERSONAJE = "com.example.myfirstapp.ID_PERSONAJE";
    private android.app.AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void irPantallaPersonaje(View view) {
        Intent intent = new Intent(this, PantallaPersonaje.class);
        Bundle b = new Bundle();
        boolean irAPantallaValida = true;
        String  name = "",
                description = "";
        switch (view.getId()) {
            case (R.id.boton_federico):
                name = getString(R.string.nombre_federico);
                description = getString(R.string.descripcion_federico);
                break;

            default:
                irAPantallaValida = false;
        }
        if (irAPantallaValida) {
            b.putCharSequence("nombre", name);
            b.putCharSequence("descripcion", description);
            intent.putExtras(b);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Ruta actualmente no disponible.");
            builder.setCancelable(true);
            currentDialog = builder.create();
            currentDialog.show();
        }
    }
}
