package com.example.ellaberinto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.ndk.base.Properties;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.microedition.khronos.egl.EGLConfig;


public class TheLabyrinth extends GvrActivity implements GvrView.StereoRenderer {

    private static final String TAG = "TheLabyrinth";

    private int objectProgram;
    private int objectPositionParam;
    private int objectUvParam;
    private int objectModelViewProjectionParam;

    private static final String OBJECT_VERTEX_SHADER_CODE =
                    "uniform mat4 u_MVP;"+
                    "attribute vec4 a_Position;"+
                    "attribute vec2 a_UV;"+
                    "varying vec2 v_UV;"+
                    ""+
                    "void main() {"+
                    "  v_UV = a_UV;"+
                    "  gl_Position = u_MVP * a_Position;"+
                    "}";

    private static final String OBJECT_FRAGMENT_SHADER_CODE =
                    "precision mediump float;"+
                    "varying vec2 v_UV;"+
                    "uniform sampler2D u_Texture;"+
                    ""+
                    "void main() {"+
                    "  // The y coordinate of this sample's textures is reversed compared to"+
                    "  // what OpenGL expects, so we invert the y coordinate."+
                    "  gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));"+
                    "}";

    private Properties gvrProperties;

    private GLSurfaceView scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();


        setContentView(R.layout.activity_the_labyrinth);
    }

    public void initializeGvrView(){

        GvrView gvrView = findViewById(R.id.gvr_view);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);
        gvrView.enableCardboardTriggerEmulation();  //in case the app runs with Daydream Headset

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
        gvrProperties = gvrView.getGvrApi().getCurrentProperties();
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE);

        objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
        objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

        Util.checkGlError("Object program params");




    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    @Override
    public void onNewFrame(HeadTransform headTransform){

    }

    @Override
    public void onDrawEye(Eye eye){

    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");


    }
}
