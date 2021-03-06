package com.example.rutashistoricas.Navegacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.hardware.SensorEventListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.rutashistoricas.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.MapboxNavigationProvider;
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback;
import com.mapbox.navigation.ui.route.NavigationMapRoute;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


/**
 * Clase correspondiente a la actividad que nos muestra el mapa junto con la ruta correspondiente del personaje seleccionado.
 *
 */
public class Mapa extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, SensorEventListener {

    /**
     * Ruta que se va a dibujar en el mapa.
     */
    private DirectionsRoute currentRoute;
    /**
     * TAG usada para imprimir logs.
     */
    private static final String TAG="DirectionsActivity";
    /**
     * Se encarga de dibujar la ruta.
     */
    private NavigationMapRoute navigationMapRoute;
    /**
     * Vista asociada a esta actividad.
     */
    private MapView mapView;
    /**
     * Gestor de permisos para poder acceder a la localización gps del dispositivo.
     */
    private PermissionsManager permissionsManager;
    /**
     * Mapa usado.
     */
    private MapboxMap mapboxMap;
    /**
     * Motor de localización que permite conocer la ubicación del dispositivo en cada momento.
     */
    private LocationEngine locationEngine = null;
    /**
     * Intervalo de tiempo entre cada consulta de la posición del dispositivo.
     */
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    /**
     * Máximo tiempo que se esperará a que el motor de localización responda con la ubicación del dispositivo.
     */
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    /**
     * Callback que se lanza cada vez que el motor de localización obtiene una nueva ubicación.
     */
    private LocationCallback callback = new LocationCallback(this);
    /**
     * Usado para obtener la ruta a partir de un conjunto de puntos por los que queremos que esta pase.
     */
    private MapboxNavigation mapboxNavigation;
    /**
     * Gestor de sensores que nos da acceso al acelerómetro.
     */
    private SensorManager sensorManager;
    /**
     * Acelerómetro.
     */
    private Sensor accelerometer;
    /**
     * Última aceleración en el eje Z detectada por el acelerómetro. Se usa para la interacción relacionada con el inicio de la navegación.
     */
    private float lastZ;
    /**
     * Nos permite saber si ya se ha iniciado la navegación por la ruta.
     */
    private boolean navegacion_iniciada = false;

    /**
     * ID del personaje que se ha seleccionado.
     */
    private int idPnj = 0;
    /**
     * ID de la ruta seleccionada.
     */
    private int idRuta = 0;

    /**
     * Contiene información relacionada con la ruta (paradas y ruta actual).
     */
    private RutaHistorica ruta;

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
     * Clase que implementa LocationEngineCallback, callback que es usado para captar las actualizaciones en la localización que detecta el motor de localización.
     *
     */
    private static class LocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<Mapa> activityWeakReference;

        LocationCallback(Mapa activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * El método de la interfaz LocationEngineCallback que se lanza cuando la localización del dispositivo ha cambiado. Dado que es una aplicación de prueba, sólo obtenemos la localización del dispositivo una vez y desactivamos este callback, ya que la navegación a través de la ruta es simulada.
         *
         * @param result El objeto LocationEngineResult que contiene la última localización conocida.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            Mapa activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                // Comentar esto para que use tu localización
                location.setLongitude(-3.607407);
                location.setLatitude(37.1702929);

                if (location == null) {
                    return;
                }

                activity.mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(result.getLastLocation().getLatitude(),
                                result.getLastLocation().getLongitude()
                        )
                ));

                activity.locationEngine.removeLocationUpdates(this);
                //activity.mapboxMap.getLocationComponent().setLocationComponentEnabled(false);

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * El método de la interfaz LocationEngineCallback que se lanza cuando la localización del dispositivo no puede ser capturada.
         *
         * @param exception La excepción lanzada.
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            Mapa activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Se ejecuta al crear la actividad. Obtiene el acceso a la API de MapBox. Pone el layout. Crea la vista del mapa ({@link #mapView}). Inicializa el acelerómetro.
     * Inicializa el reconocedor de voz y el botón asociado a este.
     * Inicializa {@link #textToSpeechEngine} y le asigna el idioma español.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titulo = "";
        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            idPnj = b.getInt("idPnj");
            idRuta = b.getInt("idRuta");
            switch (idPnj) {
                case 1:
                    switch (idRuta) {
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
        }

        ruta = new RutaHistorica(idPnj, idRuta);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_mapa);

        Log.d("Franprueba", "quepasa");

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Log.d("Franprueba", "quepasa");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("f","f");
        }

        lastZ=0;
        setTitle(titulo);

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
     * Se ejecuta una vez el mapa ha sido cargado. Almacena la referencia al mapa (en {@link #mapboxMap}) y configura el estilo del mapa. Una vez el estilo está cargado, habilita la localización del dispositivo ({@link #enableLocationComponent}) y dibuja la ruta en el mapa (en caso de que la localización se halla podido habilitar correctamente, {@link #dibujarRuta}).
     *
     * @param mapboxMap Mapa que acaba de ser cargado.
     */
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;


        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                , new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);


                        if(locationEngine!=null) {
                            dibujarRuta(1, style);
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Mapa.this);
                            builder1.setMessage("Agita el móvil hacia arriba y abajo para iniciar la navegación.");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            builder1.create().show();
                        }
                    }
                });
    }


    /**
     * Dibuja la ruta en el mapa, iniciando dicha ruta en la localización actual del dispositivo.
     *
     * @param index Índice de la ruta que será pintada en el mapa. El índice 1 corresponde a la ruta de Federico García Lorca.
     * @param style Estilo del mapa que se está usando.
     */
    private void dibujarRuta(int index, Style style) {

        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        Point origin = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());

        List<Point> paradas = ruta.getParadas();
        paradas.add(0,origin);

        List<Feature> markerCoordinates = new ArrayList<>();
        for(int i=1; i<paradas.size(); i++) {
            markerCoordinates.add(Feature.fromGeometry( paradas.get(i) ));
        }

        style.addSource(new GeoJsonSource("marker-source", FeatureCollection.fromFeatures(markerCoordinates)));

        style.addImage("marker-image", BitmapFactory.decodeResource(this.getResources(),R.drawable.mapbox_marker_icon_default));

        style.addLayer(new SymbolLayer("marker-layer", "marker-source").withProperties(PropertyFactory.iconImage("marker-image"), iconAllowOverlap(true)));

        mapboxNavigation.requestRoutes(
                RouteOptions.builder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .coordinates(paradas)
                        .profile(DirectionsCriteria.PROFILE_WALKING)
                        .baseUrl(Constants.BASE_API_URL)
                        .user(DirectionsCriteria.PROFILE_DEFAULT_USER)
                        .requestUuid("uuid")
                        .bannerInstructions(false)
                        .voiceInstructions(false)
                        .build()
                , new RoutesRequestCallback() {
                    @Override
                    public void onRoutesReady(@NotNull List<? extends DirectionsRoute> list) {
                        currentRoute = list.get(0);
                        ruta.setDirectionsRoute(currentRoute);
                        if (navigationMapRoute != null)
                            navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onRoutesRequestFailure(@NotNull Throwable throwable, @NotNull RouteOptions routeOptions) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }

                    @Override
                    public void onRoutesRequestCanceled(@NotNull RouteOptions routeOptions) {
                        Toast.makeText(Mapa.this, "error", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }


    /**
     * Si la aplicación tiene permisos para acceder a la localización, habilita el componente de localización del mapa e inicia el motor de localización (llamando al método {@link #initLocationEngine}). Si no tiene dichos permisos crea el gestor de permisos y los solicita.
     *
     * @param loadedMapStyle Estilo del mapa que se está usando.
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this,loadedMapStyle).useDefaultLocationEngine(false).build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.NONE);
            locationComponent.setRenderMode(RenderMode.NORMAL);
            initLocationEngine();

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Configura el motor de localización. Después inicializa {@link #mapboxNavigation} y {@link #navigationMapRoute}.
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);

        NavigationOptions navOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(this,getString(R.string.mapbox_access_token))
                .locationEngine(locationEngine)
                .build();
        mapboxNavigation= MapboxNavigationProvider.create(navOptions);
        navigationMapRoute = new NavigationMapRoute.Builder(mapView, this.mapboxMap, this)
                .withVanishRouteLineEnabled(true)
                .withMapboxNavigation(mapboxNavigation)
                .build();
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
                        iniciaNavegacion();
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
        String text="Las opciones disponibles son Iniciar la ruta o retroceder a la pantalla anterior.";
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
                if ( cad.indexOf("atras") != -1 || cad.indexOf("retroced") != -1 ) {
                    return 0;
                } else if (  ( cad.indexOf("inicia") != -1 ) && ( cad.indexOf("ruta") != -1 ) ) {
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

    /**
     * Usado durante la petición de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Se usa para mostrar un mensaje cuando se están concediendo los permisos de localización.
     */
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    /**
     * Se ejecuta cuando le concedemos o denegamos los permisos de localización a la aplicación. En caso de concedérselos habilita el componente de localización y dibuja la ruta ({@link #enableLocationComponent}, {@link #dibujarRuta}) y en caso de denegarselos imprime un mensaje y finaliza.
     */
    @Override
    public void onPermissionResult(boolean granted) {
        Log.d("Franprueba", "hola");
        if (granted) {
            mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                    , new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            enableLocationComponent(style);


                            if(locationEngine!=null) {
                                dibujarRuta(1, style);
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(Mapa.this);
                                builder1.setMessage("Agita el móvil hacia arriba y abajo para iniciar la navegación.");
                                builder1.setCancelable(true);

                                builder1.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                builder1.create().show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        navegacion_iniciada = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (mapboxNavigation != null) {
            mapboxNavigation.onDestroy();
        }
    }

    /**
     * Se ejecuta cada vez que el acelerómetro detecta un cambio. Si no hemos iniciado aún la navegación ({@link Navegador}) y el movimiento del dispositivo es suficientemente grande se lanzará la actividad de navegación por la ruta.
     *
     * @param event Evento que almacena la aceleración detectada.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float deltaZ=Math.abs(lastZ-event.values[2]);
        if(deltaZ>15.0f && !navegacion_iniciada){
            iniciaNavegacion();
        }
        lastZ=event.values[2];
    }

    /**
     * Si no hemos iniciado aún la navegación ({@link Navegador}) se lanzará la actividad de navegación por la ruta.
     */
    public void iniciaNavegacion(){
        if (!navegacion_iniciada){
            navegacion_iniciada = true;
            Intent intent = new Intent(Mapa.this, Navegador.class);
            intent.putExtra("rutaHistorica", ruta);
            startActivity(intent);
            Toast.makeText(this, "Iniciando navegación", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
