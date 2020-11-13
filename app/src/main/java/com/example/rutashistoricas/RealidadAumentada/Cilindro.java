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


public class Cilindro {

    private FloatBuffer mFVertexBuffer;
    private ByteBuffer mIndexBuffer;
    private float[] vertices = new float[126];
    private byte[] indices = new byte[42];
    private float[] textureCoordinates = new float[84];

    // Our UV texture buffer.
    private FloatBuffer mTextureBuffer;

    // Our texture id.
    private int mTextureId = -1;

    // The bitmap we want to load as a texture.
    private Bitmap mBitmap;

    // Indicates if we need to load the texture.
    private boolean mShouldLoadTexture = false;

    public Cilindro(Context context) {
        /*vertices[0] = 20.0f;
        vertices[1] = 0.0f;
        vertices[2] = -10.0f;
        indices[0] = (byte) (0);
        textureCoordinates[0] = 1.0f;
        textureCoordinates[1] = 1.0f;

        vertices[3] = 20.0f;
        vertices[4] = 0.0f;
        vertices[5] = 10.0f;
        indices[1] = (byte) (1);
        textureCoordinates[2] = 1.0f;
        textureCoordinates[3] = 0.0f;*/

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

    public void draw(GL10 gl) {

        /* Smooth color
        if (mColorBuffer != null) {
            // Enable the color array buffer to be used during rendering.
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        }*/

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

    private static FloatBuffer makeFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    protected void setTextureCoordinates(float[] textureCoords) {
        // float is 4 bytes, therefore we multiply the number if
        // vertices with 4.
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(
                textureCoords.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTextureBuffer = byteBuf.asFloatBuffer();
        mTextureBuffer.put(textureCoords);
        mTextureBuffer.position(0);
    }

    public void loadBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        mShouldLoadTexture = true;
    }

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

