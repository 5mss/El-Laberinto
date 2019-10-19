package com.example.ellaberinto;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

public class MazeMap {

    private final static String TAG = "MazeMap";
    int m, n;
    public boolean[][] map;
    public ArrayList<Integer> path = new ArrayList<Integer>();
    public ArrayList<Integer> wall = new ArrayList<Integer>();

    public MazeMap(Context context, String fileName, int width, int length){

        m = width; n = length;
        map = new boolean[m][n];

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
            int i, j;
            for (i=0;i<width;i++)
            {
                String str = br.readLine();
                for (j = 0; j < length; j++)
                {
                    if(str.charAt(j) == '0')
                    {
                        path.add(i);
                        path.add(j);
                    }
                    else
                    {
                        map[i][j] = true;
                        wall.add(i);
                        wall.add(j);
                    }
                }
            }
        }catch (FileNotFoundException e) {
            System.out.println(TAG + ":" + ":Exception thrown  :" + e);
        }
        catch (IOException e){
            System.out.println(TAG + ":" + ":Exception thrown  :" + e);
        }



    }

}
