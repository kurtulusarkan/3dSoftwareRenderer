package com.github.kurtulusarkan.softwarerenderer.tga;

/**
 * Created by kurtulusarkan on 1/23/17.
 */
public class TGAColor {

    final int bytesPerPixel;
    final byte[] color;

    public TGAColor() {
        bytesPerPixel = 3;
        color = new byte[4];
    }

    public TGAColor(byte[] color, int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
        this.color = color;
    }
}
