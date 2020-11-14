package com.example.rutashistoricas.RealidadAumentada;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Cilindro shape;

    private static float[] new_vectorUp = {0.0f,0.0f,-10.0f};
    private static float[] current_vectorUp = {0.0f,0.0f,-10.0f};
    private static float[] new_point = {0.0f,0.0f,-10.0f};
    private static float[] current_point = {0.0f,0.0f,-10.0f};
    private static float alfa = 0.5f;
    private static float norm_point = 0.1f, norm_vect = 0.1f;
    private static float distance_points;

    public MyGLRenderer(Context context) {
        shape = new Cilindro(context);
    }

    public void asignarDatosSensor(float[] angles, float[] p){
        new_point = p;

        new_vectorUp[0] = (float) (Math.sin(angles[0])*Math.cos(angles[1]));
        new_vectorUp[1] = (float) (Math.cos(angles[0])*Math.cos(angles[1]));
        new_vectorUp[2] = (float) (-Math.sin(angles[1]));

    }


    @Override
    public void onDrawFrame(GL10 gl) {
        distance_points = distance(current_point, new_point);

        if (distance_points>0.05) {
            //alfa = (float) Math.exp(-Math.pow(distance_points-0.1,2)/4)/2;
            current_point[0] = (0.1f) * new_point[0]/norm(new_point) + current_point[0];
            current_point[1] = (0.1f) * new_point[1]/norm(new_point) + current_point[1];
            current_point[2] = (0.1f) * new_point[2]/norm(new_point) + current_point[2];
            norm_point = norm(current_point);
            current_point[0] /= norm_point;
            current_point[1] /= norm_point;
            current_point[2] /= norm_point;
        }

        distance_points = distance(current_vectorUp, new_vectorUp);

        if (distance_points>0.05) {
            //alfa = (float) Math.exp(-Math.pow(distance_points-0.1,2)/4)/2;
            current_vectorUp[0] = (0.1f) * new_vectorUp[0]/norm(new_vectorUp) + current_vectorUp[0];
            current_vectorUp[1] = (0.1f) * new_vectorUp[1]/norm(new_vectorUp) + current_vectorUp[1];
            current_vectorUp[2] = (0.1f) * new_vectorUp[2]/norm(new_vectorUp) + current_vectorUp[2];
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

    public float distance(float[] a, float[] b){
        if (a.length!=3 || b.length!=3){
            return 0.0f;
        }
        return (float) Math.pow(Math.pow(a[0]-b[0], 2) + Math.pow(a[1]-b[1], 2) + Math.pow(a[2]-b[2], 2), 0.5);
    }

    public float norm(float[] a){
        return distance(a, new float[]{0.0f,0.0f,0.0f});
    }


}
