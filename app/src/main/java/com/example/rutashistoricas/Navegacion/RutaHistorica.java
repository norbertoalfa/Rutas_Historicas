package com.example.rutashistoricas.Navegacion;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase usada para almacenar información de la ruta que ha sido seleccionada en el menú de selección de ruta.
 */
public class RutaHistorica implements Serializable {

    /**
     * Lista de paradas de la ruta (se almacenan sus coordenadas).
     */
    private List<Point> paradas = null;
    /**
     * Lista de paradas de la ruta (se almacenan sus nombres).
     */
    private List<String> nombresParadas = null;

    /**
     * Lista de curiosidades de la ruta (se almacenan sus coordenadas).
     */
    private List<Point> curiosidades = null;
    /**
     * Lista de curiosidades de la ruta (se almacenan sus nombres).
     */
    private List<String> nombresCuriosidades = null;

    /**
     * Ruta de direcciones obtenida a partir de las paradas, usando la API de Mapbox.
     */
    private DirectionsRoute directionsRoute = null;

    /**
     * ID del personaje asociado a esta ruta.
     */
    private int idPnj;
    /**
     * ID de la ruta.
     */
    private int idRuta;

    /**
     * Constructor de la clase. Crea los vectores {@link #paradas} y {@link #nombresParadas} y los inicializa con las paradas que tendrá la ruta,
     * la cuál viene deteminada por su ID de personaje e ID de ruta (pasados como argumento).
     *
     * @param id_pnj ID del personaje asociado a la ruta.
     * @param id_ruta ID de la ruta.
     */
    public RutaHistorica(int id_pnj, int id_ruta) {
        idPnj = id_pnj;
        idRuta = id_ruta;

        paradas = new ArrayList<>();
        nombresParadas = new ArrayList<>();
        curiosidades = new ArrayList<>();
        nombresCuriosidades = new ArrayList<>();

        switch (idPnj) {
            case 1:
                switch (idRuta) {
                    case 1:
                        nombresParadas.add("Huerta de San Vicente");
                        paradas.add(Point.fromLngLat(-3.609268, 37.170675));

                        nombresParadas.add("Centro García Lorca");
                        paradas.add(Point.fromLngLat(-3.600693, 37.176633));

                        //nombresParadas.add("Catedral");
                        //paradas.add(Point.fromLngLat( -3.600633, 37.176316));

                        nombresParadas.add("Mirador de San Nicolás");
                        paradas.add(Point.fromLngLat( -3.592665, 37.181105));

                        //nombresParadas.add("Monasterio de San Jerónimo");
                        //paradas.add(Point.fromLngLat(-3.602936, 37.179895));

                        nombresParadas.add("Monumento a Federico García Lorca");
                        paradas.add(Point.fromLngLat(-3.602994, 37.183474));

                        nombresCuriosidades.add("Facultad de Traducción e Interpretación");
                        curiosidades.add(Point.fromLngLat(-3.603626, 37.175497)); //37.175497

                        nombresCuriosidades.add("Catedral");
                        curiosidades.add(Point.fromLngLat( -3.600633, 37.176316));

                        nombresCuriosidades.add("Camino Nuevo de San Nicolás");
                        curiosidades.add(Point.fromLngLat( -3.593710, 37.180936));

                        break;
                }
        }
    }

    /**
     * Getter de {@link #paradas}.
     *
     * @return Devuelve el campo {@link #paradas}.
     */
    public List<Point> getParadas() {

        return paradas;
    }

    /**
     * Getter de {@link #nombresParadas}.
     *
     * @return Devuelve el campo {@link #nombresParadas}.
     */
    public List<String> getNombresParadas() {
        return nombresParadas;
    }

    /**
     * Devuelve el elemento i-ésimo del vector {@link #nombresParadas}.
     *
     * @return i-ésimo elemento del vector {@link #nombresParadas}.
     */
    public String getNombreParada(int index) {
        return nombresParadas.get(index);
    }

    /**
     * Getter de {@link #curiosidades}.
     *
     * @return Devuelve el campo {@link #curiosidades}.
     */
    public List<Point> getCuriosidades() {
        return curiosidades;
    }

    /**
     * Getter de {@link #nombresCuriosidades}.
     *
     * @return Devuelve el campo {@link #nombresCuriosidades}.
     */
    public List<String> getNombresCuriosidades() {
        return nombresCuriosidades;
    }

    /**
     * Setter de {@link #directionsRoute}.
     *
     * @param dirRoute Objeto que será almacenado en el campo {@link #directionsRoute}.
     */
    public void setDirectionsRoute(DirectionsRoute dirRoute) {
        directionsRoute = dirRoute;
    }

    /**
     * Getter de {@link #directionsRoute}.
     *
     * @return Devuelve el campo {@link #directionsRoute}.
     */
    public DirectionsRoute getDirectionsRoute() {
        return directionsRoute;
    }

    /**
     * Getter de {@link #idPnj}.
     *
     * @return Devuelve el ID del personaje asociado a esta ruta.
     */
    public int getIdPnj() {
        return idPnj;
    }

    /**
     * Getter de {@link #idRuta}.
     *
     * @return Devuelve el ID de esta ruta.
     */
    public int getIdRuta() {
        return idRuta;
    }
}
