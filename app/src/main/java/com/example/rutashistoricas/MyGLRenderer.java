package com.example.rutashistoricas;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Triangle shape;

    private float n = 0;
    private float[] orientationAngles = {0.0f,0.0f,1.0f};
    private float[] vectorUp = {0.0f,0.0f,-10.0f};
    private float[] vectorRight = {0.0f,0.0f,-10.0f};
    private float[] point = {0.0f,0.0f,-10.0f};

    public MyGLRenderer(Context context) {
        shape = new Triangle(context);
    }

    public void asignarDatosSensor(float[] angles, float[] p){
        orientationAngles = angles;
        point = p;

        vectorUp[0] = (float) (-Math.sin(-angles[0])*Math.cos(angles[1]));
        vectorUp[1] = (float) (-Math.cos(-angles[0])*Math.cos(angles[1]));
        vectorUp[2] = (float) (-Math.sin(angles[1]));

    }


    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        /*gl.glTranslatef(orientationAngles[0], orientationAngles[1], orientationAngles[2]);
        gl.glRotatef((float)(orientationAngles[0]*180.f/Math.PI), 0.0f, 0.0f, 1.0f);
        gl.glRotatef((float)(orientationAngles[1]*180.f/Math.PI), 1.0f, 0.0f, 0.0f);
        gl.glRotatef((float)(-orientationAngles[2]*180.f/Math.PI), 0.0f, 1.0f, 0.0f);
        */
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        GLU.gluLookAt(gl,0.0f, 0.0f, 0.0f, point[0], point[1], point[2],vectorUp[0], vectorUp[1], vectorUp[2]);

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

        gl.glClearColor(0, 0, 0, 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CCW);

        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

}
