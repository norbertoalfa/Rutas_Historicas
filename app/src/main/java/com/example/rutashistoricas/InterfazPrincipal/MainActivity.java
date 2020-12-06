package com.example.rutashistoricas.InterfazPrincipal;

import android.Manifest;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.example.rutashistoricas.Navegacion.Mapa;
import com.example.rutashistoricas.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Clase correspondiente a la actividad principal que nos permite buscar y seleccionar personajes.
 *
 */
public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    private FloatingActionButton micButton;
    private Intent speechRecognizerIntent;
    public static final Integer RecordAudioRequestCode = 1;

    private boolean escuchando = false;
    private int idPnj;

    /**
     * Se ejecuta al crear la actividad.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        micButton = findViewById(R.id.micButton);

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
                //Toast.makeText(MainActivity.this, "Escuchando", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onRmsChanged(float v) {
            }
            @Override
            public void onBufferReceived(byte[] bytes) {
            }
            @Override
            public void onEndOfSpeech() {
            }
            @Override
            public void onError(int i) {
            }
            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                float[]  scores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                //Toast.makeText(MainActivity.this, data.get(0), Toast.LENGTH_SHORT).show();

                idPnj = reconocerPersonaje(data, scores);

                String texto="Creo que te refieres a ";
                switch (idPnj){
                    case 1:
                        texto = texto + "Federico García Lorca";
                        break;
                    case 2:
                        texto = texto + "Mariana Pineda";
                        break;
                    case 3:
                        texto = texto + "Ángel Ganivet";
                        break;
                    default:
                        texto = "No te he entendido";
                }


                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage(texto);
                builder1.setCancelable(true);

                if (idPnj != 0) {
                builder1.setPositiveButton(
                        "Sí",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                irPantallaPersonaje(idPnj);
                                dialog.cancel();
                            }
                        });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                } else {
                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                }

                builder1.create().show();

            }
            @Override
            public void onPartialResults(Bundle bundle) {
            }
            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
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
        idPnj = 0;
        switch (view.getId()) {
            case (R.id.boton_federico):
                idPnj = 1;
                break;
            default:
                irAPantallaValida = false;
        }
        irPantallaPersonaje(idPnj);
    }

    private void irPantallaPersonaje(int id_pnj) {
        boolean irAPantallaValida = true;
        if (id_pnj != 1) {
            irAPantallaValida = false;
        }
        if (irAPantallaValida) {
            Intent intent = new Intent(this, PantallaPersonaje.class);
            Bundle b = new Bundle();
            b.putInt("idPnj", id_pnj);
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

    public void voiceButton(View view) {
        if (escuchando) {
            escuchando = false;
            micButton.setImageResource(R.drawable.ic_mic_black_off);
            speechRecognizer.stopListening();
        } else {
            escuchando = true;
            micButton.setImageResource(R.drawable.ic_mic_black_24dp);
            speechRecognizer.startListening(speechRecognizerIntent);

        }
    }

    private int reconocerPersonaje(ArrayList<String> data, float[] scores) {
        int size = data.size();

        for (int i=0; i<size; i++) {
            if ( scores[i] > 0.6 ) {
                if ( data.get(i).indexOf("Federico") != -1 ) {
                    return 1;
                } else if ( data.get(i).indexOf("Mariana") != -1 ) {
                    return 2;
                } else if ( data.get(i).indexOf("Ganivet") != -1 ) {
                    return 3;
                }
            }
        }

        return 0;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"PermissionGranted",Toast.LENGTH_SHORT).show();
        }
    }

}
