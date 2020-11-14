package com.example.rutashistoricas.Navegacion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.rutashistoricas.InterfazPrincipal.ListadoRutas;
import com.example.rutashistoricas.R;
import com.example.rutashistoricas.RealidadAumentada.RealidadAumentada;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.navigation.base.trip.model.RouteLegProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.arrival.ArrivalController;
import com.mapbox.navigation.core.arrival.ArrivalOptions;
import com.mapbox.navigation.ui.NavigationView;
import com.mapbox.navigation.ui.NavigationViewOptions;
import com.mapbox.navigation.ui.OnNavigationReadyCallback;
import com.mapbox.navigation.ui.listeners.NavigationListener;
import com.mapbox.navigation.ui.map.NavigationMapboxMap;

import org.jetbrains.annotations.NotNull;

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
     * Dialog que se está mostrando en pantalla (en caso de que lo haya).
     */
    private AlertDialog currentDialog = null;

    /**
     * Botón que nos permite continuar la ruta cuando llegamos a un punto de interés.
     */
    private Button botonContinuarRuta = null;

    /**
     * Se ejecuta al crear la actividad. Obtiene la información referente a la ruta, que es enviada desde la actividad {@link Mapa}. Pone el título de la ruta.
     * Obtiene el acceso a la API de MapBox. Pone el layout, crea la vista y la inicializa. Almacena la ruta y deshabilita el botón {@link #botonContinuarRuta}.
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

        botonContinuarRuta =(Button) findViewById(R.id.button3);
        botonContinuarRuta.setVisibility(View.INVISIBLE);
        botonContinuarRuta.setEnabled(false);

        View instrucciones=findViewById(R.id.instructionView);
        instrucciones.setVisibility(View.INVISIBLE);
        instrucciones.setEnabled(false);

    }

    /**
     * Controla cómo reacciona la aplicación cuando se llega a una parada. Para la única ruta que está implementada (ruta de Granada ciudad de Federico),
     * en la primera parada (Huerta de San Vicente) lanza la actividad de realidad aumentada ({@link com.example.rutashistoricas.RealidadAumentada.RealidadAumentada}).
     * En el resto de paradas lanza un mensaje de que la acrividad aún no ha sido implementada.
     */
    private ArrivalController arrivalController = new ArrivalController() {
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
                int indexPto = routeLegProgress.getLegIndex() + 1;
                switch (indexPto) {
                    case 1:
                        Intent intent = new Intent(Navegador.this, RealidadAumentada.class);
                        //dialog.cancel();
                        intent.putExtra("indexPuntoInteres", indexPto);
                        intent.putExtra("rutaHistorica", ruta);
                        startActivity(intent);
                        break;
                    default:
                        AlertDialog.Builder builder = new AlertDialog.Builder(Navegador.this);
                        builder.setMessage(getString(R.string.func_no_prog));
                        builder.setCancelable(true);
                        currentDialog = builder.create();
                        currentDialog.show();
                        break;
                }

                botonContinuarRuta.setVisibility(View.VISIBLE);
                botonContinuarRuta.setEnabled(true);
            }
            return false;
        }
    };

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
     * Método lanzado al pulsar el botón {@link #botonContinuarRuta}. Se continúa la navegación hacia
     * la siguiente parada, se deshabilita el botón y se hace saber a la actividad que ya no estamos en
     * un punto de interés.
     *
     * @param view Vista del botón.
     */
    public void continueRoute(View view){
        puntoInteresLanzado=false;
        mapboxNavigation.navigateNextRouteLeg();
        botonContinuarRuta.setVisibility(View.INVISIBLE);
        botonContinuarRuta.setEnabled(false);
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

