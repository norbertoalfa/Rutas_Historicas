package com.example.rutashistoricas;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
/*
public class MyGLRenderer implements  GLSurfaceView.Renderer {
    private Triangle mTriangle;

    @Override
    public void onSurfaceCreated(GL10 unused, javax.microedition.khronos.egl.EGLConfig config) {
        mTriangle = new Triangle();
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 0.0f);
    }

    public void onDrawFrame(GL10 unused) {
        mTriangle.draw();
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

}*/

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Camera camera;
    private SurfaceTexture texture;

    private Triangle shape;
    private float n = 0;
    private float[] orientationAngles = {0.0f,0.0f,1.0f};
    private float[] vectorUp = {0.0f,0.0f,-10.0f};
    private float[] vectorRight = {0.0f,0.0f,-10.0f};
    private float[] point = {0.0f,0.0f,-10.0f};

    public MyGLRenderer() {
        shape = new Triangle();
        //camera = Camera.open(1);
    }

    public void asignarDatosSensor(float[] angles, float[] p){
        orientationAngles = angles;
        point = p;

        /*vectorRight[0] = (float) (10.0f*Math.cos(datos[0]+Math.PI/2)*Math.cos(datos[2]));
        vectorRight[2] = (float) (10.0f*Math.sin(datos[0]+Math.PI/2)*Math.cos(datos[2]));
        vectorRight[1] = (float) (10.0f*Math.sin(datos[2]));

        vectorUp[0] = (float) (10.0f*Math.cos(datos[0])*Math.cos(datos[1]));
        vectorUp[2] = (float) (10.0f*Math.sin(datos[0])*Math.cos(datos[1]));
        vectorUp[1] = (float) (10.0f*Math.sin(datos[1]));

        point[0] = vectorUp[1]*vectorRight[2] - vectorUp[2]*vectorRight[1];
        point[1] = vectorUp[2]*vectorRight[0] - vectorUp[0]*vectorRight[2];
        point[2] = vectorUp[0]*vectorRight[1] - vectorUp[1]*vectorRight[0];*/


        vectorUp[0] = (float) (-Math.sin(angles[0])*Math.cos(angles[1]));
        vectorUp[1] = (float) (-Math.cos(angles[0])*Math.cos(angles[1]));
        vectorUp[2] = (float) (-Math.sin(angles[1]));

        /*
        vectorUp[0] = 0.0f;
        vectorUp[2] = 0.0f;
        vectorUp[1] = 1.0f;
        */
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        //camera.startPreview();
        //camera.setPreviewTexture(texture);
        //camera.setOneShotPreviewCallback(camera.setPreviewCallback());

        gl.glMatrixMode(GL11.GL_MODELVIEW);
        gl.glLoadIdentity();

        //gl.glTranslatef(orientationAngles[0], orientationAngles[1], orientationAngles[2]);
        /*gl.glRotatef((float)(orientationAngles[0]*180.f/Math.PI), 0.0f, 0.0f, 1.0f);
        gl.glRotatef((float)(orientationAngles[1]*180.f/Math.PI), 1.0f, 0.0f, 0.0f);
        gl.glRotatef((float)(-orientationAngles[2]*180.f/Math.PI), 0.0f, 1.0f, 0.0f);
        */
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        GLU.gluLookAt(gl,0.0f, 0.0f, 0.0f, point[0], point[1], point[2],vectorUp[0], vectorUp[1], vectorUp[2]);

        shape.draw(gl);

        gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        gl.glViewport(0, 0, width, height);

        float ratio;
        float zNear = .1f;
        float zFar = 1000f;
        float fieldOfView = (float) Math.toRadians(30);
        float size;

        gl.glEnable(GL11.GL_NORMALIZE);

        ratio = (float) width / (float) height;

        gl.glMatrixMode(GL11.GL_PROJECTION);

        size = zNear * (float) (Math.tan((double) (fieldOfView / 2.0f)));

        gl.glFrustumf(-size, size, -size / ratio, size / ratio, zNear, zFar);

        gl.glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        gl.glDisable(GL11.GL_DITHER);

        gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);

        gl.glClearColor(0, 0, 0, 0);

        gl.glEnable(GL11.GL_CULL_FACE);
        gl.glFrontFace(GL11.GL_CCW);

        gl.glShadeModel(GL11.GL_SMOOTH);

        gl.glEnable(GL11.GL_DEPTH_TEST);
    }

}
