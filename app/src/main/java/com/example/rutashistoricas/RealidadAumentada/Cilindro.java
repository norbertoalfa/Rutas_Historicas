package com.example.rutashistoricas.RealidadAumentada;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import com.example.rutashistoricas.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 *  Clase cuyos objetos representan cilindros con textura (foto 360).
 */
public class Cilindro {
    /**
     *  Buffer de los vértices del cilindro.
     */
    private FloatBuffer mFVertexBuffer;
    /**
     *  Buffer de los índices de los puntos (orden en el que se dibujan los triángulos y los puntos
     * con los que se hacen).
     */
    private ByteBuffer mIndexBuffer;
    /**
     *  Buffer de la textura.
     */
    private FloatBuffer mTextureBuffer;
    /**
     *  Vértices del cilindro (son 42, pero tienen 3 coordenadas cada uno, ya que es en 3D).
     */
    private float[] vertices = new float[126];
    /**
     *  Coordenadas de textura de cada vértice (son 42 pero tienen 2 componentes, las texturas son en
     * 2D).
     */
    private float[] textureCoordinates = new float[84];
    /**
     *  Índices de los puntos (orden en el que se dibujan los triángulos y los puntos con los que se
     * hacen).
     */
    private byte[] indices = new byte[42];
    /**
     *  Identificador de la textura.
     */
    private int mTextureId = -1;
    /**
     *  Mapa de bits que queremos que contenga la textura.
     */
    private Bitmap mBitmap;
    /**
     *  Variable que indica si es necesario volver a cargar la textura.
     */
    private boolean mShouldLoadTexture = false;

    /**
     *  Se ejecuta al crear el objeto Cilindro. Asigna los valores de los vértices, índices y
     * coordenadas de textura. Genera espacio para guardar los datos en la GPU, mediante los buffers.
     * Carga la textura.
     *
     * @param context Contexto de la actividad que ha creado el objeto.
     */
    public Cilindro(Context context) {

        for (int i=0; i<21; i++){
            vertices[6*i]   = (float) (20*Math.cos(2*i*Math.PI/20));
            vertices[6*i+1] = (float) (20*Math.sin(2*i*Math.PI/20));
            vertices[6*i+2] = -10.0f;
            indices[2*i] = (byte) (2*i);
            textureCoordinates[4*i] = (float) (0.05*(20-i));
            textureCoordinates[4*i+1] = 1.0f;

            vertices[6*i+3] = (float) (20*Math.cos(2*i*Math.PI/20));
            vertices[6*i+4] = (float) (20*Math.sin(2*i*Math.PI/20));
            vertices[6*i+5] = 10.0f;
            indices[2*i+1] = (byte) (2*i+1);
            textureCoordinates[4*i+2] = (float) (0.05*(20-i));
            textureCoordinates[4*i+3] = 0.0f;
        }

        mFVertexBuffer = makeFloatBuffer(vertices);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

        loadBitmap(BitmapFactory.decodeResource( context.getResources(), R.drawable.casa_federico_360));
        setTextureCoordinates(textureCoordinates);
    }

    /**
     *  Se ejecuta cuando se quiera visualizar el objeto. Carga la textura si es necesario y procede
     * a la visualización de los vértices siguiendo el patrón indicado por los índices y añadiendo la
     * textura según se le indique con las coordenadas de textura.
     *
     * @param gl Interfaz GL usada.
     */
    public void draw(GL10 gl) {

        if (mShouldLoadTexture) {
            loadGLTexture(gl);
            mShouldLoadTexture = false;
        }
        if (mTextureId != -1 && mTextureBuffer != null) {
            gl.glEnable(GL10.GL_TEXTURE_2D);
            // Enable the texture state
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            // Point to our buffers
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        }

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer); //
        // Point out the where the color buffer is.
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, indices.length);

        if (mTextureId != -1 && mTextureBuffer != null) {
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }

    }

    /**
     *  Reserva un espacio en la memoria de la GPU a modo de buffer y lo devuelve.
     *
     * @param arr Vector que se quiere añadir a la memoria de la GPU mediante un buffer.
     *
     * @return Devuelve el buffer creado para el vector de entrada.
     */
    private static FloatBuffer makeFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    /**
     *  Reserva memoria en la GPU a modo de buffer para las coordenadas de textura.
     *
     * @param textureCoords Vector de coordenadas de textura a asignar.
     */
    protected void setTextureCoordinates(float[] textureCoords) {
        // float is 4 bytes, therefore we multiply the number if
        // vertices with 4.
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCoords.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTextureBuffer = byteBuf.asFloatBuffer();
        mTextureBuffer.put(textureCoords);
        mTextureBuffer.position(0);
    }

    /**
     *  Se actualiza el mapa de bits que se quiere utilizar como textura.
     *
     * @param bitmap Mapa de bits a utilizar.
     */
    public void loadBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        mShouldLoadTexture = true;
    }

    /**
     *  Asigna las coordenadas de textura y visualiza la textura en el entorno 3D.
     *
     * @param gl Interfaz GL usada.
     */
    private void loadGLTexture(GL10 gl) {
        // Generate one texture pointer...
        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureId = textures[0];

        // ...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);

        // Create Nearest Filtered Texture
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        // Different possible texture parameters, e.g. GL11.GL_CLAMP_TO_EDGE
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT);

        // Use the Android GLUtils to specify a two-dimensional texture image
        // from our bitmap
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
    }
}

