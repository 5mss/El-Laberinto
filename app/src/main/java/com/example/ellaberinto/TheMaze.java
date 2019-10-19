package com.example.ellaberinto;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.vr.ndk.base.Properties;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;


public class TheMaze extends GvrActivity implements GvrView.StereoRenderer {

    private static final String TAG = "TheMaze";

    private int objectProgram;
    private int objectPositionParam;
    private int objectUvParam;
    private int objectModelViewProjectionParam;

    private float[] camera;
    private float[] cameraPosition;
    private float[] view;
    private float[] headDirection;
    private float[] positionM;
    private float[] modelViewProjection;
    private float[] modelView;
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 10.0f;

    private TexturedMesh box;
    private TexturedMesh tileUp;
    private TexturedMesh tileDown;
    private Texture wallTexture;
    private Texture floorTexture;
    private Texture ceilingTexture;
    private float[][] wallPositions;
    private float[][] floorPositions;
    private float[][] ceilingPositions;
    private int wallNumber;
    private int pathNumber;
    private String wallTextureFileName = "wallTexture1.png";
    private String floorTextureFileName = "floorTexture1.png";
    private String ceilingTextureFileName = "ceilingTexture1.png";
    private final static float wallHeight = 2.0f;
    private final static float eyeHeight = 1.5f;
    private final static float edge = 1.0f;


    private MazeMap mazeMap;
    private float[] startingP = {1.5f, eyeHeight, 1.5f};  // camera starting point
    private int[] destination = {8, 1};  // map index of destination

    private final static float speed = 4.0f;
    private float[] velocity = new float[2];
    long startTime, endTime;
    private boolean isWalking = false;
    private boolean isTouching = false;

    private static final String[] OBJECT_VERTEX_SHADER_CODE =
            new String[] {
                    "uniform mat4 u_MVP;",
                    "attribute vec4 a_Position;",
                    "attribute vec2 a_UV;",
                    "varying vec2 v_UV;",
                    "",
                    "void main() {",
                    "  v_UV = a_UV;",
                    "  gl_Position = u_MVP * a_Position;",
                    "}",
            };
    private static final String[] OBJECT_FRAGMENT_SHADER_CODE =
            new String[] {
                    "precision mediump float;",
                    "varying vec2 v_UV;",
                    "uniform sampler2D u_Texture;",
                    "",
                    "void main() {",
                    "  // The y coordinate of this sample's textures is reversed compared to",
                    "  // what OpenGL expects, so we invert the y coordinate.",
                    "  gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));",
                    "}",
            };


    private Properties gvrProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {  //
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        initializeGvrView();

        mazeMap = new MazeMap(this, "maze", 10,10);
        wallNumber = mazeMap.wall.size()/2;
        pathNumber = mazeMap.path.size()/2;
        wallPositions = new float[wallNumber][3];
        floorPositions = new float[pathNumber][3];
        ceilingPositions = new float[pathNumber][3];

        int i=0;
        for(i=0;i<wallNumber;i++)
        {
            wallPositions[i][0] = mazeMap.wall.get(2 * i +1).floatValue() + 0.5f;
            wallPositions[i][1] = 0;
            wallPositions[i][2] = mazeMap.wall.get(2 * i).floatValue() + 0.5f;
        }
        for(i=0;i<pathNumber;i++)
        {
            floorPositions[i][0] = mazeMap.path.get(2 * i +1 ).floatValue() +0.5f;
            floorPositions[i][1] = 0;
            floorPositions[i][2] = mazeMap.path.get(2 * i).floatValue() + 0.5f;
            ceilingPositions[i][0] = mazeMap.path.get(2 * i +1 ).floatValue() +0.5f;
            ceilingPositions[i][1] = wallHeight;
            ceilingPositions[i][2] = mazeMap.path.get(2 * i).floatValue() + 0.5f;
        }

        camera = new float[16];
        cameraPosition = startingP;
        headDirection = new float[3];
        view = new float[16];
        positionM = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
    }

    public void initializeGvrView(){
        setContentView(R.layout.activity_the_labyrinth);

        GvrView gvrView = findViewById(R.id.gvr_view);
        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);
        gvrView.enableCardboardTriggerEmulation();  //in case the app runs with Daydream Headset
        gvrView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouching = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouching = false;
                        isWalking = false;
                        break;
                        default:
                            isTouching = false;
                            isWalking = false;
                }
                return true;
            }
        });

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
    public void onSurfaceCreated(EGLConfig config) {  //
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE);

        objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
        objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

        Util.checkGlError("Object program params");

        Matrix.setLookAtM(camera, 0, cameraPosition[0], cameraPosition[1], cameraPosition[2], cameraPosition[0] + 1.0f, cameraPosition[1], cameraPosition[2], 0.0f, 1.0f, 0.0f);
        try
        {
            box = new TexturedMesh(this, "cube.obj", objectPositionParam, objectUvParam);
            tileUp = new TexturedMesh(this, "squareUp.obj", objectPositionParam, objectUvParam);
            tileDown = new TexturedMesh(this, "squareDown.obj", objectPositionParam, objectUvParam);
            wallTexture = new Texture(this, wallTextureFileName);
            floorTexture = new Texture(this, floorTextureFileName);
            ceilingTexture = new Texture(this, ceilingTextureFileName);
        }catch (IOException e)
        {
            System.out.println(TAG + ":" + "cube.obj" + ":Exception thrown  :" + e);

        }

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    @Override
    public void onNewFrame(HeadTransform headTransform){
        startTime = System.currentTimeMillis();
        headTransform.getForwardVector(headDirection, 0);

        if(isTouching)
            checkWalking();

        if(isWalking) {
            // update camera position
            if (velocity[0] != 0)
                cameraPosition[0] = cameraPosition[0] + velocity[0] * 0.001f * (startTime - endTime);
            if (velocity[1] != 0)
                cameraPosition[2] = cameraPosition[2] + velocity[1] * 0.001f * (startTime - endTime);

            // update camera matrix
            Matrix.setLookAtM(camera, 0, cameraPosition[0], cameraPosition[1], cameraPosition[2], cameraPosition[0] + 1.0f, cameraPosition[1], cameraPosition[2], 0.0f, 1.0f, 0.0f);
            isWalking = false;
        }
        endTime = startTime;

        Util.checkGlError("onNewFrame");
    }

    @Override
    public void onDrawEye(Eye eye){
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        int i=0;
        for(i=0;i<wallNumber;i++)
            drawMesh(box, wallTexture, perspective, wallPositions[i][0], wallPositions[i][1], wallPositions[i][2]);
        for(i=0;i<pathNumber;i++)
        {
            drawMesh(tileUp, floorTexture, perspective, floorPositions[i][0], floorPositions[i][1], floorPositions[i][2]);
            drawMesh(tileDown, ceilingTexture, perspective, ceilingPositions[i][0], ceilingPositions[i][1], ceilingPositions[i][2]);
        }

    }

    public void drawMesh(TexturedMesh mesh, Texture texture, float[] perspective, float x, float y, float z)
    {
        Matrix.setIdentityM(positionM, 0);
        Matrix.translateM(positionM, 0, x, y, z);
        Matrix.multiplyMM(modelView, 0, view, 0, positionM, 0);
        Matrix.multiplyMM(modelViewProjection, 0 , perspective, 0, modelView, 0);
        GLES20.glUseProgram(objectProgram);
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
        texture.bind();
        mesh.draw();
        Util.checkGlError("drawMesh");
    }

    @Override
    public void onFinishFrame(Viewport viewport) { }

    public void checkWalking()
    {

        float x = headDirection[0], z = headDirection[2];
        float L = (float)Math.sqrt(x * x + z * z);
        velocity[0] = speed * x / L;  // compute walking velocity according to head orientation
        velocity[1] = speed * z / L;
        // check whether walking in either of the directions will cause collision and reset velocity
        int i = (int)cameraPosition[2], j = (int)cameraPosition[0], iSign = Util.sign(velocity[1]), jSign = Util.sign(velocity[0]);
        int iMove = i + iSign, jMove = j+jSign;
        if(jMove<0||jMove>mazeMap.n||mazeMap.map[i][jMove])
        {
            if((jSign<0&&(cameraPosition[0]-j<=0.5))||(jSign>0&&(cameraPosition[0]-j>0.5)))
                velocity[0] = 0f;
        }
        if(iMove<0||iMove>mazeMap.m||mazeMap.map[iMove][j])
        {
            if((iSign<0&&(cameraPosition[2]-i<=0.5))||(iSign>0&&(cameraPosition[2]-i>0.5)))
                velocity[1] = 0f;
        }
        // if velocity is not 0, start walking
        if(velocity[0] != 0 || velocity[1] != 0) {
            isWalking = true;
        }
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }
}
