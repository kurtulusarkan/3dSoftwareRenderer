package com.github.kurtulusarkan.softwarerenderer.tga;

import java.util.Arrays;

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

    public TGAColor(int blue, int green, int red, int alpha, int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
        color = new byte[4];
        color[0] = (byte) blue;
        color[1] = (byte) green;
        color[2] = (byte) red;
        color[3] = (byte) alpha;
    }

    public TGAColor(byte[] color, int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
        this.color = color;
    }

    @Override
    public String toString() {
        return "TGAColor{" +
                "bytesPerPixel=" + bytesPerPixel +
                ", color=" + Arrays.toString(color) +
                '}';
    }
}
