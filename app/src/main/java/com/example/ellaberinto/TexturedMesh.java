package com.example.ellaberinto;

import android.content.Context;
import android.opengl.GLES20;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class TexturedMesh
{
    private static final String TAG = "TexturedMesh";

    private final FloatBuffer vertices;
    private final FloatBuffer uv;
    private final ShortBuffer indices;
    private final int positionAttrib;
    private final int uvAttrib;

    public TexturedMesh(Context context, String objPath, int positionAttrib, int uvAttrib) throws IOException
    {
        // load from obj file
        InputStream objInputStream = context.getAssets().open(objPath);
        Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
        objInputStream.close();

        IntBuffer intIndices = ObjData.getFaceVertexIndices(obj, 3);
        vertices = ObjData.getVertices(obj);
        uv = ObjData.getTexCoords(obj, 2);

        // Convert int indices to shorts (GLES doesn't support int indices)
        indices =
                ByteBuffer.allocateDirect(2 * intIndices.limit())
                        .order(ByteOrder.nativeOrder())
                        .asShortBuffer();
        while (intIndices.hasRemaining()) {
            indices.put((short) intIndices.get());
        }
        indices.rewind();

        this.positionAttrib = positionAttrib;
        this.uvAttrib = uvAttrib;
    }

    public void draw() {
        GLES20.glEnableVertexAttribArray(positionAttrib);
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, vertices);
        GLES20.glEnableVertexAttribArray(uvAttrib);
        GLES20.glVertexAttribPointer(uvAttrib, 2, GLES20.GL_FLOAT, false, 0, uv);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.limit(), GLES20.GL_UNSIGNED_SHORT, indices);
    }

}
