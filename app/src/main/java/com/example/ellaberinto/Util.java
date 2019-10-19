package com.example.ellaberinto;

import android.opengl.GLES20;
import android.text.TextUtils;
import android.util.Log;

import static android.opengl.GLU.gluErrorString;

public class Util {

    private static final String TAG = "Util";

    private static final boolean HALT_ON_GL_ERROR = true;

    public static void checkGlError(String label) {
        int error = GLES20.glGetError();
        int lastError;
        if (error != GLES20.GL_NO_ERROR) {
            do {
                lastError = error;
                Log.e(TAG, label + ": glError " + gluErrorString(lastError));
                error = GLES20.glGetError();
            } while (error != GLES20.GL_NO_ERROR);

            if (HALT_ON_GL_ERROR) {
                throw new RuntimeException("glError " + gluErrorString(lastError));
            }
        }
    }

    public static int compileProgram(String[] vertexCode, String[] fragmentCode) {

        checkGlError("Start of compileProgram");
        // prepare shaders and OpenGL program
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, TextUtils.join("\n", vertexCode));
        GLES20.glCompileShader(vertexShader);
        checkGlError("Start of compileProgram");

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, TextUtils.join("\n", fragmentCode));
        GLES20.glCompileShader(fragmentShader);
        checkGlError("Compile fragment shader");

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        // Link and check for errors.
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String errorMsg = "Unable to link shader program: \n" + GLES20.glGetProgramInfoLog(program);
            Log.e(TAG, errorMsg);
            if (HALT_ON_GL_ERROR) {
                throw new RuntimeException(errorMsg);
            }
        }

        return program;
    }

    public static int sign(float x)
    {
        return x>0?1:-1;
    }

}
