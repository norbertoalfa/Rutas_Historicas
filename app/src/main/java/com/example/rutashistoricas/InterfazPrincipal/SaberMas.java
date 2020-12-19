package com.example.rutashistoricas.InterfazPrincipal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rutashistoricas.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Clase correspondiente a la actividad que muestra más información acerca de un personaje concreto.
 * Además, esta clase también permite abrir una url que nos lleva a una página web relacionada
 * con el personaje.
 *
 */
public class SaberMas extends AppCompatActivity {
    /**
     * Para gestión interna de los eventos táctiles. Nos permite saber a que velocidad se mueven los punteros por la pantalla durante un evento táctil.
     */
    private VelocityTracker mVelocityTracker = null;

    /**
     * Para gestión interna de los eventos táctiles. ID correspondiente a un puntero que produce un evento táctil.
     */
    private int mActivePointerId1;
    /**
     * Para gestión interna de los eventos táctiles. ID correspondiente a un puntero que produce un evento táctil.
     */
    private int mActivePointerId2;

    /**
     * ID del personaje para el cual se están visualizando las rutas.
     */
    private static int idPnj = 0;

    /**
     * Nombre de la actividad, que será el nombre del personaje cuya biografía se está mostrando.
     */
    private static String nombre = "";

    /**
     * Biografía del personaje.
     */
    private static String biografia = "";

    /**
     * Nos proporciona acceso al servicio de reconocimiento de voz.
     */
    private SpeechRecognizer speechRecognizer;

    /**
     * Botón (desplazable) del micrófono. Al pulsarlo se activa el reconocedor de voz.
     */
    private FloatingActionButton micButton;

    /**
     * Intent asociado al reconocedor de voz.
     */
    private Intent speechRecognizerIntent;

    /**
     * Nos permite saber si está activo el reconocedor de voz.
     */
    boolean escuchando = false;

    /**
     * Motor utilizado para decir mediante voz un String.
     */
    private TextToSpeech textToSpeechEngine;



    /**
     * Se ejecuta al crear la actividad. Obtiene el ID del personaje seleccionado, que es enviado por la actividad {@link PantallaPersonaje}
     * (actividad padre de esta).
     * Inicializa los campos de texto del layout con el nombre del personaje y la biografía asociados a dicho ID.
     * A continuación, asocia una url con más información del personaje al botón de maś información,
     * de forma que cuando se hace click en él, se abre la url en un navegador.
     * Inicializa el reconocedor de voz y el botón asociado a este.
     * Inicializa {@link #textToSpeechEngine} y le asigna el idioma español.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saber_mas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("idPnj");
            switch (idPnj) {
                case 1:
                    nombre = getString(R.string.nombre_federico);
                    biografia = getString(R.string.biografia_federico);
            }
        }

        setTitle(nombre);
        TextView textView = findViewById(R.id.biografia);
        textView.setText(biografia);


        Button botonLink = findViewById(R.id.botonLink);

        String url = getString(R.string.url_federico);

        botonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        micButton = findViewById(R.id.micButton);
        iniciarSpeechRecognizer();

        Locale spanish = new Locale("es", "ES");

        textToSpeechEngine= new TextToSpeech(this,new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.setLanguage(spanish);
                }
            }
        });
    }

    /**
     * Se ejecuta cuando se produce un evento táctil. Se encarga de comprobar si se hace un movimiento con dos dedos desplazándose
     * por la pantalla de izquierda a derecha, y en caso de producirse finaliza la actividad para volver a su actividad padre.
     *
     * @param event Evento táctil.
     *
     * @return Devuelve siempre true.
     */
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

    /**
     * Inicializa el servicio de reconocimiento de voz.
     * Establece el Listener que se usará cuando el reconocimiento de voz sea activado (es decir, cuando el botón {@link #micButton} sea pulsado).
     * Cuando el reconocedor obtenga un resultado se llamará al método {@link #reconocer}, que analizará el resultado obtenido. La aplicación
     * reaccionará de diferentes formas en función de lo que el usuario haya dicho.
     */
    private void iniciarSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

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
                int id_opcion = -1;

                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                float[] scores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                //Toast.makeText(MainActivity.this, data.get(0), Toast.LENGTH_SHORT).show();

                id_opcion = reconocer(data, scores);

                switch (id_opcion) {
                    case -1:
                        break;
                    case 0:
                        finish();
                        break;
                    case 1:
                        String url = getString(R.string.url_federico);
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                    case 2:
                        decirOpciones();
                        break;
                }

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
     * Método que utiliza {@link #textToSpeechEngine} para decir las opciones de voz disponibles para el usuario.
     */
    public void decirOpciones(){
        String text="Las opciones disponibles son Saber más o retroceder a la pantalla anterior.";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeechEngine.speak(text,TextToSpeech.QUEUE_FLUSH,null,"tts1");
        }
        else{
            textToSpeechEngine.speak(text,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    /**
     * Se encarga de analizar el resultado que el reconocedor de voz haya percibido.
     *
     * @param data Array con el texto asociado a las palabras que el reconocedor de voz ha percibido.
     * @param scores Porcentaje de seguridad con el que el reconocedor ha percibido cada String.
     * @return Entero que nos permite identificar si el usuario ha dicho algo que deba provocar un cambio en la aplicación.
     */
    private int reconocer(ArrayList<String> data, float[] scores) {
        int size = data.size();
        String cad = "";

        for (int i=0; i<size; i++) {
            if ( scores[i] > 0.6 ) {
                cad = "";
                cad = data.get(i).toLowerCase();
                cad = Normalizer.normalize(cad, Normalizer.Form.NFD);
                cad = cad.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                Log.d("FRANPRUEBA", cad);
                if ( cad.indexOf("atras") != -1 || cad.indexOf("retroced") != -1 ) {
                    return 0;
                } else if ( cad.indexOf("saber mas") != -1 || cad.indexOf("mas informacion") != -1 ) {
                    return 1;
                } else if ( cad.indexOf("opciones") != -1 ){
                    return 2;
                }
            }
        }

        return -1;
    }

    /**
     * Método lanzado al pulsar el botón del micrófono, el cuál activa el reconocimiento de voz o lo desactiva si ya estaba activo.
     *
     * @param view Vista del botón.
     */
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

}