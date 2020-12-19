package com.example.rutashistoricas.InterfazPrincipal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import com.example.rutashistoricas.Navegacion.Mapa;
import com.example.rutashistoricas.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Clase correspondiente a la actividad que muestra las rutas de un personaje concreto.
 * Desde ella se puede seleccionar una ruta, la cual será mostrada en el mapa, o acceder a más
 * información acerca de la ruta.
 *
 */
public class ListadoRutas extends AppCompatActivity {
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
     * ID de la ruta seleccionada
     */
    private static int idRuta = 0;

    /**
     * Cuadro de diálogo que muestra que la funcionalidad a la que se quiere acceder no está implementada.
     */
    AlertDialog dialogoFuncionalidad = null;
    /**
     * Cuadro de diálogo que indica que los servicios de localización no están activos.
     */
    AlertDialog localizationDialog = null;

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

    private TextToSpeech textToSpeechEngine;


    /**
     * Se ejecuta al crear la actividad. Obtiene el ID del personaje seleccionado, que es enviado por la actividad {@link PantallaPersonaje}
     * (actividad padre de esta).
     * Inicializa los campos de texto del layout con el nombre de las rutas y el nombre del personaje asociados a dicho ID.
     * Inicializa el reconocedor de voz y el botón asociado a este.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_rutas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        String titulo_ruta_1 = "";
        String texto_ruta_2 = "";
        String texto_ruta_3 = "";
        String name = "";

        if (b != null) {
            idPnj = b.getInt("idPnj");
            switch (idPnj) {
                case 1:
                    name = getString(R.string.nombre_federico);
                    titulo_ruta_1 = getString(R.string.federico_titulo_ruta_1);
                    texto_ruta_2 = getString(R.string.federico_titulo_ruta_2);
                    texto_ruta_3 = getString(R.string.federico_titulo_ruta_3);
                    break;
                default:
                    break;
            }
        }

        setTitle(name);
        TextView textView;

        textView = findViewById(R.id.tituloRuta1);
        textView.setText(titulo_ruta_1);
        textView.setAllCaps(true);
        textView = findViewById(R.id.tituloRuta2);
        textView.setText(texto_ruta_2);
        textView.setAllCaps(true);
        textView = findViewById(R.id.tituloRuta3);
        textView.setText(texto_ruta_3);
        textView.setAllCaps(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(ListadoRutas.this);
        builder.setMessage("El servicio de localización está desactivado. Para poder iniciar la ruta debes activarlo.");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        localizationDialog = builder.create();

        builder.setMessage(getString(R.string.func_no_prog));
        //builder.setCancelable(true);
        dialogoFuncionalidad = builder.create();

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

                //Toast.makeText(ListadoRutas.this, data.get(0), Toast.LENGTH_SHORT).show();
                //Log.d("FRANPRUEBA", data.get(0));

                idRuta = -1;
                id_opcion = reconocer(data, scores);

                switch (id_opcion) {
                    case -1:
                        break;
                    case 0:
                        finish();
                        break;
                    case 1:
                        iniciarRuta(idRuta);
                        break;
                    case 2:
                        infoRuta(idRuta);
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

    public void decirOpciones(){
        String text="Las opciones disponibles son iniciar una de las tres rutas, mostrar información de una de las tres rutas o retroceder a la pantalla anterior.";
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
        int size = data.size(), ret = -1;
        String cad = "";

        for (int i=0; i<size; i++) {
            if ( scores[i] > 0.6 ) {
                cad = "";
                cad = data.get(i).toLowerCase();
                cad = Normalizer.normalize(cad, Normalizer.Form.NFD);
                cad = cad.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                //Log.d("FRANPRUEBA", cad);
                if ( cad.indexOf("atras") != -1 || cad.indexOf("retroced") != -1 ) {
                    return 0;
                } else if (  ( cad.indexOf("inicia") != -1 ) && ( cad.indexOf("ruta") != -1 ) ) {
                    if ( cad.indexOf("granada ciudad") != -1 ) {
                        idRuta = 1;
                    } else if ( cad.indexOf("agua") != -1 ) {
                        idRuta = 2;
                    } else if ( cad.indexOf("viznar y alfacar") != -1 ) {
                        idRuta = 3;
                    }
                    if (idRuta != -1) {
                        ret = 1;
                    }
                } else if ( ( cad.indexOf("muestra") != -1 || cad.indexOf("mostrar") != -1 ) &&  cad.indexOf("informacion") != -1  &&  cad.indexOf("ruta") != -1 ) {
                    if ( cad.indexOf("granada ciudad") != -1 ) {
                        idRuta = 1;
                    } else if ( cad.indexOf("agua") != -1 ) {
                        idRuta = 2;
                    } else if ( cad.indexOf("viznar y alfacar") != -1 ) {
                        idRuta = 3;
                    }
                    if (idRuta != -1) {
                        ret = 2;
                    }
                } else if ( cad.indexOf("opciones") != -1 ){
                    return 3;
                }
            }
        }

        return ret;
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
     * Si el ID de ruta que recibe como parámetro es 1, lanza la actividad {@link com.example.rutashistoricas.Navegacion.Mapa}
     * y le envía el ID del personaje y el ID de la ruta. En otro caso, dado que sólo está implementada la ruta 1, informa de
     * que la ruta seleccionada no está implementada.
     *
     * @param i ID de la ruta que se va a iniciar.
     */
    private void iniciarRuta(int i) {
        if (i!= 1) {
            dialogoFuncionalidad.show();
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Bundle b = new Bundle();
            b.putInt("idPnj", idPnj);
            b.putInt("idRuta", i);
            Intent intent = new Intent(this, Mapa.class);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                intent.putExtras(b);
                startActivity(intent);
            } else {
                localizationDialog.show();
            }
        }
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a iniciar la primera ruta.
     * Lanza la actividad {@link com.example.rutashistoricas.Navegacion.Mapa} y le envía el ID del personaje
     * y el ID de la ruta, en este caso 1.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void iniciarRuta1(View view) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 1);
        Intent intent = new Intent(this, Mapa.class);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            intent.putExtras(b);
            startActivity(intent);
        } else {
            localizationDialog.show();
        }
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a iniciar la segunda ruta.
     * Esta ruta no está implementada e informa de ello por pantalla.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void iniciarRuta2(View view) {
        dialogoFuncionalidad.show();

        // No borrar este comentario
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 2);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a iniciar la primera ruta.
     * Esta ruta no está implementada y por tanto esta función no hace nada.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void iniciarRuta3(View view) {
        dialogoFuncionalidad.show();

        // No borrar este comentario
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 3);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

    /**
     * Método que nos debería mostrar información de la ruta correspondiente al ID que recibe como parámetro.
     * Dado que esta opción no está implementada, este método avisa de ello por pantalla.
     *
     * @param i ID de la ruta.
     */
    private void infoRuta(int i) {
        dialogoFuncionalidad.show();
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a obtener información de la primera ruta.
     * Dado que esta opción no está implementada, este método avisa de ello por pantalla.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void infoRuta1(View view) {
        dialogoFuncionalidad.show();

        // No borrar este comentario
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 3);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a obtener información de la primera ruta.
     * Dado que esta opción no está implementada, este método avisa de ello por pantalla.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void infoRuta2(View view) {
        dialogoFuncionalidad.show();

        // No borrar este comentario
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 3);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

    /**
     * Método ejecutado al pulsar el botón correspondiente a obtener información de la primera ruta.
     * Dado que esta opción no está implementada, este método avisa de ello por pantalla.
     *
     * @param view Vista del botón que se ha pulsado.
     */
    public void infoRuta3(View view) {
        dialogoFuncionalidad.show();

        // No borrar este comentario
        /*
        Bundle b = new Bundle();
        b.putInt("idPnj", idPnj);
        b.putInt("idRuta", 3);
        Intent intent = new Intent(this, Mapa.class);
        intent.putExtras(b);
        startActivity(intent);
         */
    }

}
