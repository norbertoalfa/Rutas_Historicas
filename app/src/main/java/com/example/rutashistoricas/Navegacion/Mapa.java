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
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.rutashistoricas.R;
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
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


/**
 * Clase correspondiente a la actividad que nos muestra el mapa junto con la ruta correspondiente al personaje seleccionado.
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
                location.setLongitude(-3.609903);
                location.setLatitude(37.174295);

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
     * Obtiene el acceso a la API de MapBox. Pone el layout. Crea la vista la vista del mapa ({@link #mapView}). Inicializa el acelerómetro.
     *
     * @param savedInstanceState Conjunto de datos del estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_mapa);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("f","f");
            // fai! we dont have an accelerometer!
        }

        lastZ=0;
        setTitle("Ruta 1");
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

                        //mapboxMap.addOnMapClickListener(MainActivity.this);

                        /*
                        button=findViewById(R.id.startButton);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MainActivity.this, Navegador.class);
                                startActivity(intent);
                            }
                        });
                        */

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

                        /*
                        button.setEnabled(true);
                        button.setBackgroundResource(R.color.mapboxBlue);
                        button.setAlpha(1);
                         */
                    }
                });
    }


    /**
     * Dibuja la ruta en el mapa, iniciando dicha ruta en la localización actual del dispositivo.
     *
     * @param index Índice de la ruta que será pintada en el mapa. El índice 1 corresponde a la ruta de Federico García Lorca.
     * @param style Estilo del mapa que se está usando.
     */
    public void dibujarRuta(int index, Style style) {

        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        Point origin = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());

        List<Point> ruta = Routes.getRoute(index);
        ruta.add(0,origin);

        List<Feature> markerCoordinates = new ArrayList<>();
        for(int i=1; i<ruta.size(); i++) {
            markerCoordinates.add(Feature.fromGeometry( ruta.get(i) ));
        }

        style.addSource(new GeoJsonSource("marker-source", FeatureCollection.fromFeatures(markerCoordinates)));

        style.addImage("marker-image", BitmapFactory.decodeResource(this.getResources(),R.drawable.mapbox_marker_icon_default));

        style.addLayer(new SymbolLayer("marker-layer", "marker-source").withProperties(PropertyFactory.iconImage("marker-image"), iconAllowOverlap(true)));

        mapboxNavigation.requestRoutes(
                RouteOptions.builder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .coordinates(ruta)
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
                        Routes.setCurrentDirectionsRoute(currentRoute);
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
                    LocationComponentActivationOptions.builder(this,loadedMapStyle)
                            .useDefaultLocationEngine(false).build();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    /**
     * Se ejecuta cuando le concedemos o denegamos los permisos de localización a la aplicación. En caso de concederselos habilita el componente de localización y dibuja la ruta ({@link #enableLocationComponent}, {@link #dibujarRuta}) y en caso de denegarselos imprime un mensaje y finaliza.
     */
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
                dibujarRuta(1,mapboxMap.getStyle());
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
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
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
        mapView.onDestroy();
        mapboxNavigation.onDestroy();
    }

    /**
     * Se ejecuta cada vez que el acelerómetro detecta un cambio. Si no hemos iniciado aún la navegación y el movimiento del dispositivo es suficientemente grande se lanzará la actividad de navegación por la ruta.
     *
     * @param event Evento que almacena la aceleración detectada.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float deltaZ=Math.abs(lastZ-event.values[2]);
        if(deltaZ>15.0f && !navegacion_iniciada){
            navegacion_iniciada = true;
            Intent intent = new Intent(Mapa.this, Navegador.class);
            startActivity(intent);
            Toast.makeText(this, "Iniciando navegación", Toast.LENGTH_LONG).show();
            /*
            if(deltaZ<-12.0f){
                //mapboxNavigation.navigateNextRouteLeg();
                currentDialog.show();
                Toast.makeText(this, "Hacia arriba", Toast.LENGTH_LONG).show();
            }
            else if(deltaZ>12.0f){
                currentDialog.cancel();
                showingDialog=false;
                Toast.makeText(this, "Hacia abajo", Toast.LENGTH_LONG).show();
            }
             */
        }
        lastZ=event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
