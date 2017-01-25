package com.github.kurtulusarkan.softwarerenderer;

import com.github.kurtulusarkan.softwarerenderer.tga.TGAColor;
import com.github.kurtulusarkan.softwarerenderer.tga.TGAImage;

import java.io.IOException;

/**
 * Created by kurtulusarkan on 1/22/17.
 */
public class SoftwareRenderer {

    public static void main(String[] args) throws IOException {

        TGAColor b = new TGAColor(255, 0, 0, 255, 3);


        // testing tga file read/write ops.
        TGAImage image = new TGAImage(100, 100, 3);

        image.drawLine(13, 20, 80, 40, b);
        image.drawLine(20, 13, 40, 80, b);

        image.writeToFile("output1.tga", false);
        image.writeToFile("output2.tga", true);
    }
}
