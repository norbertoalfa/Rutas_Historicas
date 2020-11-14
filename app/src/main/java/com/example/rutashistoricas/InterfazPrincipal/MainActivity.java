package com.example.rutashistoricas.InterfazPrincipal;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.rutashistoricas.R;

public class MainActivity extends AppCompatActivity {
    public static final String ID_PERSONAJE = "com.example.myfirstapp.ID_PERSONAJE";
    private android.app.AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void irPantallaPersonaje(View view) {
        boolean irAPantallaValida = true;
        int idPnj = 0;
        switch (view.getId()) {
            case (R.id.boton_federico):
                idPnj = 1;
                break;
            default:
                irAPantallaValida = false;
        }
        if (irAPantallaValida) {
            Intent intent = new Intent(this, PantallaPersonaje.class);
            Bundle b = new Bundle();
            b.putInt("idPnj", idPnj);
            intent.putExtras(b);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Personaje actualmente no disponible.");
            builder.setCancelable(true);
            currentDialog = builder.create();
            currentDialog.show();
        }
    }
}
