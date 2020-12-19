package com.example.rutashistoricas.Navegacion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rutashistoricas.InterfazPrincipal.ListadoRutas;
import com.example.rutashistoricas.R;
import com.example.rutashistoricas.RealidadAumentada.RealidadAumentada;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.navigation.base.trip.model.RouteLegProgress;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.arrival.ArrivalController;
import com.mapbox.navigation.core.arrival.ArrivalOptions;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.ui.NavigationView;
import com.mapbox.navigation.ui.NavigationViewOptions;
import com.mapbox.navigation.ui.OnNavigationReadyCallback;
import com.mapbox.navigation.ui.listeners.NavigationListener;
import com.mapbox.navigation.ui.map.NavigationMapboxMap;

import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Clase correspondiente a la actividad de navegación que nos guía por el mapa.
 */
public class Navegador extends AppCompatActivity implements OnNavigationReadyCallback, NavigationListener {

    /**
     * Mapa usado.
     */
    private NavigationMapboxMap navigationMapBoxMap = null;

    /**
     * Nos permite personalizar la experiencia de navegación. A través de este campo interactuamos con el SDK de navegación de Mapbox.
     */
    private MapboxNavigation mapboxNavigation=null;

    /**
     * Ruta que se va a seguir durante la navegación.
     */
    private DirectionsRoute currentRoute;

    /**
     * Vista de la actividad.
     */
    private NavigationView navigationView;

    /**
     * Contiene información relacionada con la ruta (paradas y ruta actual).
     */
    private RutaHistorica ruta;

    /**
     * Se utiliza para saber si hemos llegado a un punto de interés y su actividad ha sido lanzada.
     */
    private boolean puntoInteresLanzado=false;

    /**
     * Dialog que muestra la información de error por funcionalidad no programada(en caso de que lo haya).
     */
    private AlertDialog dialogoError = null;

    /**
     * Dialog que muestra la información de la curiosidad (en caso de que lo haya).
     */
    private AlertDialog dialogoCuriosidad = null;

    /**
     * Botón que nos permite continuar la ruta cuando llegamos a un punto de interés.
     */
    private Button botonContinuarRuta = null;

    /**
     * Botón que inicia la actividad Realidad Aumentada.
     */
    private Button botonRealidadAumentada = null;

    /**
     * Botón que nos permite mostrar la información de la continuidad actual.
     */
    private Button botonCuriosidad = null;

    /**
     * Entero que identifica la curiosidad que está activa en el momento. Una curiosidad está activa si y sólo si el usuario está a menos de una distancia considerada.
     * Sólo puede haber una curiosidad activa en cada momento.
     * Si este entero tiene el valor 0 significa que no hay ninguna curiosidad activa.
     */
    private int numCuriosidadActiva = 0;

    /**
     * Entero que identifica el punto de interés en el que se encuentra el usuario. Se usa para lanzar la actividad Realidad Aumentada.
     */
    private int indexPuntoActual = 0;

    /**
     * Controla cómo reacciona la aplicación cuando se llega a una parada. Para la única ruta que está implementada (ruta de Granada ciudad de Federico),
     * en la primera parada (Huerta de San Vicente) lanza la actividad de realidad aumentada ({@link com.example.rutashistoricas.RealidadAumentada.RealidadAumentada}).
     * En el resto de paradas lanza un mensaje de que la acrividad aún no ha sido implementada.
     */
    private ArrivalController arrivalController;

    /**
     * Comprueba constantemente la distancia del usuario a los puntos de curiosidad para actualizar la información relacionada con las curiosidades.
     */
    private RouteProgressObserver routeProgressObserver;

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

    int textIdCuriosidadActiva;

    /**
     * Se ejecuta al crear la actividad. Obtiene la información referente a la ruta, que es enviada desde la actividad {@link Mapa}. Pone el título de la ruta.
     * Obtiene el acceso a la API de MapBox. Pone el layout, crea la vista y la inicializa. Almacena la ruta y deshabilita el botón {@link #botonContinuarRuta}.
     * Inicializa el reconocedor de voz y el botón asociado a este.
     * Inicializa {@link #textToSpeechEngine} y le asigna el idioma español.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titulo = "";

        ruta = (RutaHistorica) getIntent().getSerializableExtra("rutaHistorica");

        switch (ruta.getIdPnj()) {
            case 1:
                switch (ruta.getIdRuta()) {
                    case 1:
                        titulo = getString(R.string.federico_titulo_ruta_1);
                        break;
                    case 2:
                        titulo = getString(R.string.federico_titulo_ruta_2);
                        break;
                    case 3:
                        titulo = getString(R.string.federico_titulo_ruta_3);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        setTitle(titulo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Mapbox.getInstance(this,getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_navegador);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);

        currentRoute = ruta.getDirectionsRoute();

        botonContinuarRuta =(Button) findViewById(R.id.botonContinuarRuta);
        botonContinuarRuta.setVisibility(View.INVISIBLE);
        botonContinuarRuta.setEnabled(false);

        botonRealidadAumentada =(Button) findViewById(R.id.botonRealidadAumentada);
        botonRealidadAumentada.setVisibility(View.INVISIBLE);
        botonRealidadAumentada.setEnabled(false);

        botonCuriosidad = (Button) findViewById(R.id.botonCuriosidad);
        botonCuriosidad.setVisibility(View.INVISIBLE);
        botonCuriosidad.setEnabled(false);

        View instrucciones=findViewById(R.id.instructionView);
        instrucciones.setVisibility(View.INVISIBLE);
        instrucciones.setEnabled(false);

        inicializarArrivalController();
        inicializarRouteProgressObserver();

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
     * Crea e inicializa {@link #arrivalController}.
     */
    private void inicializarArrivalController() {
        arrivalController = new ArrivalController() {
            @NotNull
            @Override
            public ArrivalOptions arrivalOptions() {
                // Cuando queden menos de 5 segundos para llegar se llamará a navigatenext route leg
                return new ArrivalOptions.Builder().arrivalInSeconds(5.0).build();
            }

            @Override
            public boolean navigateNextRouteLeg(@NotNull RouteLegProgress routeLegProgress) {

                if(!puntoInteresLanzado) {
                    puntoInteresLanzado=true;
                    indexPuntoActual = routeLegProgress.getLegIndex() + 1;
                    iniciarRealidadAumentada(navigationView);

                    botonContinuarRuta.setVisibility(View.VISIBLE);
                    botonContinuarRuta.setEnabled(true);

                    botonRealidadAumentada.setVisibility(View.VISIBLE);
                    botonRealidadAumentada.setEnabled(true);
                }
                return false;
            }
        };

    }

    /**
     * Crea e inicializa {@link #routeProgressObserver}.
     */
    private void inicializarRouteProgressObserver() {
        routeProgressObserver = new RouteProgressObserver() {
            @Override
            public void onRouteProgressChanged(@NotNull RouteProgress routeProgress) {
                List<Point> curiosidades = ruta.getCuriosidades();
                MapboxMap mapboxMap = navigationMapBoxMap.retrieveMap();
                Location location;
                //Point p = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                //navigationView.addMarker(p);
                double distancia = 0;

                for(int i=0; i<curiosidades.size(); i++) {
                    Location curiosidad = new Location("curiosidad");
                    curiosidad.setLongitude(curiosidades.get(i).longitude());
                    curiosidad.setLatitude(curiosidades.get(i).latitude());
                    location = mapboxMap.getLocationComponent().getLastKnownLocation();
                    distancia = location.distanceTo(curiosidad);
                    if (distancia<85.0) {
                        numCuriosidadActiva = i+1;
                        switch (i+1) {
                            case 1:
                                textIdCuriosidadActiva = R.string.texto_curiosidad_1;
                                break;
                            case 2:
                                textIdCuriosidadActiva = R.string.texto_curiosidad_2;
                                break;
                            case 3:
                                textIdCuriosidadActiva = R.string.texto_curiosidad_3;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(Navegador.this);
                        dialogoCuriosidad = builder.setMessage(getString(textIdCuriosidadActiva))
                                            .setCancelable(true)
                                            .create();
                        botonCuriosidad.setVisibility(View.VISIBLE);
                        botonCuriosidad.setEnabled(true);

                    } else if ( (i+1) == numCuriosidadActiva) {
                        numCuriosidadActiva = 0;
                        textIdCuriosidadActiva=-1;
                        botonCuriosidad.setVisibility(View.INVISIBLE);
                        botonCuriosidad.setEnabled(false);
                    }

                }

            }
        };

    }

    /**
     * Método lanzado cuando pulsamos en la flecha de ir hacia atrás. Lanzamos de nuevo la actividad {@link Mapa}.
     *
     * @param item Item del menú que ha sido pulsado. En nuestro caso solo puede ser la flecha de ir hacia atrás.
     * @return Se devuelve siempre true.
     */
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Mapa.class);
        Bundle b = new Bundle();
        b.putInt("idPnj", ruta.getIdPnj());
        b.putInt("idRuta", ruta.getIdRuta());
        myIntent.putExtras(b);
        startActivityForResult(myIntent, 0);
        return true;
    }

    /**
     * Método lanzado al pulsar el botón {@link #botonContinuarRuta}. Llama al método {@link #continuarRuta()}.
     *
     * @param view Vista del botón.
     */
    public void continuarRuta(View view){
        continuarRuta();
    }

    /**
     * Si la ruta está parada en un punto de interés, se continúa la navegación hacia la siguiente
     * parada, se deshabilita el botón y se hace saber a la actividad que ya no estamos en un punto
     * de interés.
     */
    public void continuarRuta() {
        puntoInteresLanzado=false;
        mapboxNavigation.navigateNextRouteLeg();
        botonContinuarRuta.setVisibility(View.INVISIBLE);
        botonContinuarRuta.setEnabled(false);
        botonRealidadAumentada.setVisibility(View.INVISIBLE);
        botonRealidadAumentada.setEnabled(false);
    }


    /**
     * Método lanzado al pulsar el botón {@link #botonRealidadAumentada}. Llama al método {@link #iniciarRealidadAumentada()}
     * @param view Vista del botón.
     */
    public void iniciarRealidadAumentada(View view) {
        iniciarRealidadAumentada();
    }

    /**
     * Inicia la actividad de Realidad Aumentada.
     */
    public void iniciarRealidadAumentada() {
        switch (indexPuntoActual) {
            case 1:
                Intent intent = new Intent(Navegador.this, RealidadAumentada.class);
                //dialog.cancel();
                intent.putExtra("indexPuntoInteres", indexPuntoActual);
                intent.putExtra("rutaHistorica", ruta);
                startActivity(intent);
                break;
            default:
                AlertDialog.Builder builder = new AlertDialog.Builder(Navegador.this);
                builder.setMessage("Has llegado a " + ruta.getNombreParada(indexPuntoActual - 1) + ". La funcionalidad de este punto de interés todavía no está disponible");
                builder.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.setCancelable(true);
                dialogoError = builder.create();
                dialogoError.show();
                break;
        }
    }

    /**
     * Método lanzado al pulsar el botón {@link #botonCuriosidad}. Llama al método {@link #mostrarCuriosidad()}.
     * @param view Vista del botón.
     */
    public void mostrarCuriosidad(View view) {
        mostrarCuriosidad();
    }

    /**
     * Se muestra la información relacionada con la curiosidad activa.
     */
    public void mostrarCuriosidad() {
        dialogoCuriosidad.show();
    }

    /**
     * Método que utiliza {@link #textToSpeechEngine} para decir la curiosidad actual.
     */
    public void decirCuriosidad(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeechEngine.speak(getString(textIdCuriosidadActiva), TextToSpeech.QUEUE_FLUSH,null,"tts1");
        }
        else{
            textToSpeechEngine.speak(getString(textIdCuriosidadActiva),TextToSpeech.QUEUE_FLUSH,null);
        }
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
                        if (puntoInteresLanzado) {
                            continuarRuta();
                        }
                        break;
                    case 2:
                        if ( puntoInteresLanzado) {
                            iniciarRealidadAumentada();
                        }
                        break;
                    case 3:
                        if (numCuriosidadActiva !=0) {
                            decirCuriosidad();
                        }
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
                if ( cad.indexOf("atras") != -1 || cad.indexOf("retroced") != -1 ) {
                    return 0;
                } else if ( cad.indexOf("continua") != -1 && cad.indexOf("ruta") != -1 ) {
                    return 1;
                } else if ( cad.indexOf("realidad aumentada") != -1 ) {
                    return 2;
                } else if ( cad.indexOf("curiosidad") != -1 ) {
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

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onStart(){
        super.onStart();
        navigationView.onStart();
    }

    /**
     * Se ejecuta al volver a esta actividad (por ejemplo, desde otra actividad lanzada desde esta).
     * En caso de que hayamos vuelto a esta actividad después de haber visualizado un punto de interés,
     * habilitamos el botón de continuar ruta.
     */
    @Override
    public void onResume(){
        super.onResume();
        navigationView.onResume();
        if(puntoInteresLanzado){
            botonContinuarRuta.setVisibility(View.VISIBLE);
            botonContinuarRuta.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    @Override
    public void onStop(){
        super.onStop();
        navigationView.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
        navigationView.onPause();
    }

    @Override
    public void onDestroy(){
        navigationView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle outState){
        super.onRestoreInstanceState(outState);
        navigationView.onRestoreInstanceState(outState);
    }

    /**
     * Se ejecuta cuando la navegación está preparada. Si la navegación aún no ha sido iniciada, inicializamos {@link #navigationMapBoxMap},
     * iniciamos la navegación, inicializamos {@link #mapboxNavigation} y le asociamos el controlador {@link #arrivalController}.
     *
     * @param isRunning Indica si la navegación está corriendo y no ha sido destruido por un cambio en la configuración.
     */
    @Override
    public void onNavigationReady(boolean isRunning) {
        if(!isRunning && navigationMapBoxMap==null){
            if(navigationView.retrieveNavigationMapboxMap()!=null){
                navigationMapBoxMap=navigationView.retrieveNavigationMapboxMap();
                NavigationViewOptions opts = NavigationViewOptions.builder(this)
                        .navigationListener(this)
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(true)
                        .build();
                navigationView.startNavigation(opts);
                if(navigationView.retrieveMapboxNavigation()!=null) {
                    mapboxNavigation = navigationView.retrieveMapboxNavigation();
                    mapboxNavigation.setArrivalController(arrivalController);
                    mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public void onCancelNavigation() {
        navigationView.stopNavigation();
        finish();
    }

    @Override
    public void onNavigationFinished() {
        finish();
    }

    @Override
    public void onNavigationRunning() {

    }
}

