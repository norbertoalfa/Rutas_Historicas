package com.example.rutashistoricas.RealidadAumentada;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.opengles.GL10;

/**
 * Clase responsable de generar la escena 3D con OpenGL
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    /**
     *  Objeto cilindro, que es aquel en el que se va a mostrar la textura (foto 360) del punto de
     * interés correpondiente.
     */
    private Cilindro shape;
    /**
     *  Vector que señala hacia dónde apunta la parte superior de la cámara en tiempo real (por ello
     * se llama vector arriba). Es la variable que contiene los valores exactos aportados por los
     * sensores.
     */
    private static float[] new_vectorUp = {0.0f,0.0f,-10.0f};
    /**
     *  Vector que señala hacia dónde apunta la parte superior de la cámara en el entorno 3D (por ello
     * se llama vector arriba). Es la variable que contiene el vector arriba esperado (se usa para
     * suavizar el movimiento de la cámara).
     */
    private static float[] current_vectorUp = {0.0f,0.0f,-10.0f};
    /**
     *  Punto hacia el que está mirando la cámara en tiempo real. Es la variable que contiene los
     * valores exactos aportados por los sensores.
     */
    private static float[] new_point = {0.0f,0.0f,-10.0f};
    /**
     *  Punto hacia el que está mirando la cámara en el entorno 3D. Es la variable que contiene el
     * punto esperado al que apunta la cámara (se utiliza para suavizar el movimiento de la cámara).
     */
    private static float[] current_point = {0.0f,0.0f,-10.0f};
    /**
     *  Coeficiente que regula la velocidad a la que rota la cámara, que depende del punto actual en
     * el entorno 3D y el punto real (igual con el vector arriba).
     */
    private static float alfa = 0.1f;
    /**
     *  Norma del punto a utilizar.
     */
    private static float norm_point = 0.1f;
    /**
     *  Norma del vector arriba a utilizar.
     */
    private static float norm_vect = 0.1f;
    /**
     *  Distancia entre el punto real y el utilizado en el entorno 3D.
     */
    private static float distance_points;

    /**
     *  Se ejecuta al crear el objeto. Crea el objeto cilindro.
     *
     * @param context Contexto de la actividad que lo crea.
     */
    public MyGLRenderer(Context context) {
        shape = new Cilindro(context);
    }

    /**
     *  Se ejecuta cuando la actividad que lo maneja quiere actualizar el punto y el vector arriba
     * mediante los datos obtenidos con los sensores.
     *
     * @param angles Ángulos del dispositivo con respecto al sistema de coordenadas estándar en el
     *              mundo real.
     * @param p Punto hacia el que apunta la cámara, en coordenadas (estándar) del mundo real.
     */
    public void asignarDatosSensor(float[] angles, float[] p){
        new_vectorUp[0] = (float) (Math.sin(angles[0])*Math.cos(angles[1]));
        new_vectorUp[1] = (float) (Math.cos(angles[0])*Math.cos(angles[1]));
        new_vectorUp[2] = (float) (-Math.sin(angles[1]));
        new_point = p;
    }

    /**
     *  Se ejecuta en cada frame. Actualiza, si es necesario, las coordenadas de la cámara del entorno
     * 3D (para suavizar el movimiento de la misma y usar los datos en bruto) y redibujar el objeto.
     *
     * @param gl Interfaz GL usada.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        distance_points = distance(current_point, new_point);

        if (distance_points>0.05) {
            alfa = (float) distance_points/10.0f;
            current_point[0] = alfa * new_point[0]/norm(new_point) + current_point[0];
            current_point[1] = alfa * new_point[1]/norm(new_point) + current_point[1];
            current_point[2] = alfa * new_point[2]/norm(new_point) + current_point[2];
            norm_point = norm(current_point);
            current_point[0] /= norm_point;
            current_point[1] /= norm_point;
            current_point[2] /= norm_point;
        }

        distance_points = distance(current_vectorUp, new_vectorUp);

        if (distance_points>0.05) {
            alfa = (float) distance_points/10.0f;
            current_vectorUp[0] = alfa * new_vectorUp[0]/norm(new_vectorUp) + current_vectorUp[0];
            current_vectorUp[1] = alfa * new_vectorUp[1]/norm(new_vectorUp) + current_vectorUp[1];
            current_vectorUp[2] = alfa * new_vectorUp[2]/norm(new_vectorUp) + current_vectorUp[2];
            norm_vect = norm(current_vectorUp);
            current_vectorUp[0] /= norm_vect;
            current_vectorUp[1] /= norm_vect;
            current_vectorUp[2] /= norm_vect;
        }

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        GLU.gluLookAt(gl,0.0f, 0.0f, 0.0f, current_point[0], current_point[1],
                current_point[2],current_vectorUp[0], current_vectorUp[1], current_vectorUp[2]);

        shape.draw(gl);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    /**
     *  Se ejecuta en cada vez que la superficie cambia de tamaño. Actualiza el tamaño de la pantalla
     * a mostrar (fustrum).
     *
     * @param gl Interfaz GL usada.
     * @param width Nuevo ancho de la pantalla.
     * @param height Nuevo alto de la pantalla.
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        gl.glViewport(0, 0, width, height);

        float ratio;
        float zNear = .1f;
        float zFar = 1000f;
        float fieldOfView = (float) Math.toRadians(30);
        float size;

        gl.glEnable(GL10.GL_NORMALIZE);

        ratio = (float) width / (float) height;

        gl.glMatrixMode(GL10.GL_PROJECTION);

        size = zNear * (float) (Math.tan((double) (fieldOfView / 2.0f)));

        gl.glFrustumf(-size, size, -size / ratio, size / ratio, zNear, zFar);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    /**
     *  Se ejecuta cuando la superficie se crea o se actualiza. Limpia la pantalla usando un color
     * determinado (azul simulando color del cielo) y redibuja el objeto.
     *
     * @param gl Interfaz GL usada.
     * @param config Configuración de la nueva superficie.
     */
    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        gl.glDisable(GL10.GL_DITHER);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(67.0f/255, 120.0f/255, 200.0f/255, 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CCW);

        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    /**
     *  Devuelve la distancia entre dos puntos
     *
     * @param a Punto inicial.
     * @param b Punto final.
     */
    public float distance(float[] a, float[] b){
        if (a.length!=3 || b.length!=3){
            return 0.0f;
        }
        return (float) Math.pow(Math.pow(b[0]-a[0], 2) + Math.pow(b[1]-a[1], 2) + Math.pow(b[2]-a[2], 2), 0.5);
    }

    /**
     *  Devuelve la norma de un vector.
     *
     * @param a Vector al que se le quiere calcular la norma.
     */
    public float norm(float[] a){
        return distance(a, new float[]{0.0f,0.0f,0.0f});
    }


}
