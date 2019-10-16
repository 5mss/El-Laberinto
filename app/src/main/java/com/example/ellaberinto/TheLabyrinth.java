package com.example.ellaberinto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

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

    private GLSurfaceView scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_the_labyrinth);
    }

    public void onSurfaceCreated(EGLConfig config) {

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

    }
}
