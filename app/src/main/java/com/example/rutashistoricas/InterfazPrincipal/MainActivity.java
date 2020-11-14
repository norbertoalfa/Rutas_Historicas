package com.example.rutashistoricas.InterfazPrincipal;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.rutashistoricas.R;

public class MainActivity extends AppCompatActivity {
    public static final String ID_PERSONAJE = "com.example.myfirstapp.ID_PERSONAJE";

    /**
     * Se ejecuta al crear la actividad.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Método ejecutado al pulsar el botón de un personaje.
     * Lanza la actividad {@link com.example.rutashistoricas.InterfazPrincipal.PantallaPersonaje} y le envía el ID del personaje.
     * En caso de no estar disponible el personaje seleccionado, se muestra un mensaje que informa al usuario de ello.
     *
     * @param view Vista del botón que se ha pulsado.
     */
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
            android.app.AlertDialog currentDialog = builder.create();
            currentDialog.show();
        }
    }
}
