package com.github.kurtulusarkan.softwarerenderer.obj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kurtulusarkan on 1/26/17.
 */
public class ObjModel {

    ArrayList<float[]> verts;

    ArrayList<int[]> faces;

    public ObjModel() {
        this.verts = new ArrayList<>();
        this.faces = new ArrayList<>();
    }

    public static ObjModel loadModelFromFile(String fileName) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

        ObjModel model = new ObjModel();

        String line;
        while (true) {

            line = bufferedReader.readLine();

            if (line == null)
                break;

            if (line.length() < 1)
                continue;

            if (line.equals("g 1"))
                break;

            String[] parts = line.split(" +");

            if (line.startsWith("v ")) {
                float[] verts = new float[3];
                int index = 0;
                for (int i = 1; i < parts.length; i++) {
                    float f = Float.parseFloat(parts[i]);
                    verts[i-1] = f;
                }
                model.verts.add(verts);
            }
            if (line.startsWith("f ")) {
                int[] faces = new int[3];
                for (int i = 1; i < parts.length; i++) {
                    String[] faceParts = parts[i].split("/");
                    faces[i - 1] = Integer.parseInt(faceParts[0]);
                }
                model.faces.add(faces);
            }
        }

        bufferedReader.close();

        return model;
    }

    public int numberOfVerts() {
        return verts.size();
    }

    public ArrayList<float[]> getVerts() {
        return verts;
    }

    public int numberOfFaces() {
        return faces.size();
    }

    public ArrayList<int[]> getFaces() {
        return faces;
    }
}
