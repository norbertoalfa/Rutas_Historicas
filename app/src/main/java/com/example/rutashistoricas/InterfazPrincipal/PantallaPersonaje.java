package com.example.rutashistoricas.InterfazPrincipal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

import com.example.rutashistoricas.InterfazPrincipal.ListadoRutas;
import com.example.rutashistoricas.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Clase correspondiente a la actividad que nos presenta un personaje concreto.
 * Se nos presenta una breve información del personaje y nos permite acceder a otra pantalla
 * para saber más acerca del mismo o al listado de sus rutas.
 *
 */
public class PantallaPersonaje extends AppCompatActivity {
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
     * Nombre de la actividad, que será el nombre del personaje seleccionado.
     */
    private static String nombre = "";

    /**
     * Fecha y lugar de nacimiento del personaje seleccionado.
     */
    private static String nacimiento = "";

    /**
     * Fecha y lugar de fallecimiento del personaje seleccionado.
     */
    private static String fallecimiento = "";

    /**
     * Categorías del personaje seleccionado: sus ocupaciones, movimientos a los que pertenecía...
     */
    private static String categorias = "";

    /**
     * Breve descripción del personaje seleccionado.
     */
    private static String descripcion = "";

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
     * Se ejecuta al crear la actividad. Obtiene el ID del personaje seleccionado, que es enviado por la actividad {@link com.example.rutashistoricas.InterfazPrincipal.MainActivity}
     * (actividad padre de esta).
     * Inicializa los campos de texto del layout con el nombre del personaje, su nacimiento, su fallecimiento, sus categorías y su descripción.
     * Inicializa el reconocedor de voz y el botón asociado a este.
     * Inicializa {@link #textToSpeechEngine} y le asigna el idioma español.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_personaje);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("idPnj");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    /**
     * Método ejecutado al pulsar el botón para saber más acerca del personaje.
     * Lanza la actividad {@link com.example.rutashistoricas.InterfazPrincipal.SaberMas} y le envía el ID del personaje.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void saberMas(View view) {
        saberMas();
    }

    /**
     * Lanza la actividad {@link com.example.rutashistoricas.InterfazPrincipal.SaberMas} y le envía el ID del personaje.
     */
    private void saberMas() {
        Intent intent = new Intent(this, SaberMas.class);
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        intent.putExtras(b);
        startActivity(intent);
    }

    /**
     * Método ejecutado al pulsar el botón para mostrar las rutas del personaje. Llama al método {@link #mostrarRutas()}
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void mostrarRutas(View view) {
        mostrarRutas();
    }

    /**
     * Lanza la actividad {@link com.example.rutashistoricas.InterfazPrincipal.ListadoRutas} y le envía el ID del personaje.
     */
    private void mostrarRutas() {
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);

        Intent intent = new Intent(this, ListadoRutas.class);
        intent.putExtras(b);
        startActivity(intent);
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
                        saberMas();
                        break;
                    case 2:
                        mostrarRutas();
                        break;
                    case 3:
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
        String text="Las opciones disponibles son Saber más, mostrar las rutas del personaje o retroceder a la pantalla anterior.";
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
                } else if ( cad.indexOf("saber mas") != -1 || ( (cad.indexOf("mostrar") != -1 || cad.indexOf("muestra") != -1 ) && cad.indexOf("informacion") != -1 ) ) {
                    return 1;
                } else if ( (cad.indexOf("mostrar") != -1 || cad.indexOf("muestra") != 1 ) && cad.indexOf("ruta") != -1 ) {
                    return 2;
                } else if ( cad.indexOf("opciones") != -1 ){
                    return 3;
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