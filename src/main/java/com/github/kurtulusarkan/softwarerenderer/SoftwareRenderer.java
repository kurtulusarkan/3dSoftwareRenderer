package com.github.kurtulusarkan.softwarerenderer;

import com.github.kurtulusarkan.softwarerenderer.obj.ObjModel;
import com.github.kurtulusarkan.softwarerenderer.tga.TGAColor;
import com.github.kurtulusarkan.softwarerenderer.tga.TGAHeader;
import com.github.kurtulusarkan.softwarerenderer.tga.TGAImage;

import java.io.IOException;

/**
 * Created by kurtulusarkan on 1/22/17.
 */
public class SoftwareRenderer {

    private static int getFaceIndex(int face, int numItems) {
        if (face > 0)
            return face-1;
        else
            return numItems - Math.abs(face);
    }

    public static void main(String[] args) throws IOException {

        ObjModel model = ObjModel.loadModelFromFile("data/african_head.obj");

        TGAImage image = new TGAImage(800, 800, TGAHeader.BBP_RGB);
        TGAColor white = new TGAColor(255, 255, 255, 255, TGAHeader.BBP_RGB);

        int numVerts = model.numberOfVerts();

        long l = System.currentTimeMillis();

        for (int i = 0; i < model.numberOfFaces(); i++) {

            int[] face = model.getFaces().get(i);

            for (int j = 0; j < 3; j++) {

                float[] v0 = model.getVerts().get(getFaceIndex(face[j], numVerts));
                float[] v1 = model.getVerts().get(getFaceIndex(face[(j+1)%3], numVerts));

                int x0 = (int)((v0[0]+1.0)*image.getWidth()/2.0);
                int y0 = (int)((v0[1]+1.0)*image.getHeight()/2.0);

                int x1 = (int)((v1[0]+1.0)*image.getWidth()/2.0);
                int y1 = (int)((v1[1]+1.0)*image.getHeight()/2.0);

                image.drawLine(x0, y0, x1, y1, white);
            }
        }

        image.flipVertically();

        System.out.println(System.currentTimeMillis() - l);

        image.writeToFile("output.tga", true);
    }
}
