package com.example.rutashistoricas.Navegacion;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RutaHistorica implements Serializable {

    private List<Point> paradas = null;
    private List<String> nombresParadas = null;
    private DirectionsRoute directionsRoute = null;

    private int idPnj;
    private int idRuta;

    public RutaHistorica(int id_pnj, int id_ruta) {
        idPnj = id_pnj;
        idRuta = id_ruta;

        paradas = new ArrayList<>();
        nombresParadas = new ArrayList<>();
        switch (idPnj) {
            case 1:
                switch (idRuta) {
                    case 1:
                        //Huerta de San Vicente
                        nombresParadas.add("Huerta de San Vicente");
                        paradas.add(Point.fromLngLat(-3.609268, 37.170675));
                        //Preguntamos si quiere ir a la catedral
                        nombresParadas.add("Catedral");
                        paradas.add(Point.fromLngLat( -3.600633, 37.176316));
                        //Centro García Lorca
                        nombresParadas.add("Centro García Lorca");
                        paradas.add(Point.fromLngLat(-3.600693, 37.176633));
                        //Preguntar si quiere ir al monasterio san jerónimo
                        nombresParadas.add("Monasterio de San Jerónimo");
                        paradas.add(Point.fromLngLat(-3.602936, 37.179895));
                        //Monumento a federico
                        nombresParadas.add("Monumento a Federico Gaecía Lorca");
                        paradas.add(Point.fromLngLat(-3.602994, 37.183474));
                        break;
                }
        }
    }

    public List<Point> getParadas() {

        return paradas;
    }

    public List<String> getNombresParadas() {
        return nombresParadas;
    }

    public String getNombreParada(int index) {
        return nombresParadas.get(index);
    }

    public void setDirectionsRoute(DirectionsRoute dirRoute) {
        directionsRoute = dirRoute;
    }

    public DirectionsRoute getDirectionsRoute() {
        return directionsRoute;
    }


    public int getIdPnj() {
        return idPnj;
    }

    public int getIdRuta() {
        return idRuta;
    }
}
