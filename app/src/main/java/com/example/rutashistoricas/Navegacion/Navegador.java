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
     * Contiene información relacionada con la ruta (paradas).
     */
    private boolean puntoInteresLanzado=false;

    /**
     * Contiene información relacionada con la ruta (paradas).
     */
    private AlertDialog currentDialog = null;

    /**
     * Contiene información relacionada con la ruta (paradas).
     */
    private Button botonContinuarRuta = null;


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

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Mapa.class);
        Bundle b = new Bundle();
        b.putInt("idPnj", ruta.getIdPnj());
        b.putInt("idRuta", ruta.getIdRuta());
        myIntent.putExtras(b);
        startActivityForResult(myIntent, 0);
        return true;
    }


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

