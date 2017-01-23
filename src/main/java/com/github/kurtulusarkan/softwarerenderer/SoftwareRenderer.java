package com.github.kurtulusarkan.softwarerenderer;

import com.github.kurtulusarkan.softwarerenderer.tga.TGAImage;

import java.io.IOException;

/**
 * Created by kurtulusarkan on 1/22/17.
 */
public class SoftwareRenderer {

    public static void main(String[] args) throws IOException {

        // testing tga file read/write ops.
        TGAImage image = TGAImage.readTGAImageFromFile("data/floor_diffuse.tga");
        image.scale(1200, 800);
        image.flipHorizontally();
        image.flipVertically();
        image.writeToFile("output1.tga", false);
        image.writeToFile("output2.tga", true);
    }
}
