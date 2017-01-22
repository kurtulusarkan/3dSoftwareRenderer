package com.github.kurtulusarkan.softwarerenderer.tga;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kurtulusarkan on 1/22/17.
 */
public class TGAHeader {

    // supported BYTE per pixel values. (header contains BITS per pixel.)
    static final int BBP_GRAYSCALE = 1;
    static final int BBP_RGB = 3;
    static final int BBP_RGBA = 4;

    // supported dataTypeCode values.
    static final byte DTC_UNCOMPRESSED_TRUECOLOR = (byte)2;
    static final byte DTC_UNCOMPRESSED_GRAYSCALE = (byte)3;
    static final byte DTC_RLE_TRUECOLOR = (byte)10;
    static final byte DTC_RLE_GRAYSCALE = (byte)11;

    byte idLength;
    byte colorMapType;
    byte dataTypeCode;
    short colorMapOrigin;
    short colorMapLength;
    byte colorMapDepth;
    short xOrigin;
    short yOrigin;
    short width;
    short height;
    byte bitsPerPixel;
    byte imageDescriptor;

    TGAHeader(ByteBuffer byteBuffer) {

        byteBuffer.rewind();

        idLength = byteBuffer.get();
        colorMapType = byteBuffer.get();
        dataTypeCode = byteBuffer.get();
        colorMapOrigin = byteBuffer.getShort();
        colorMapLength = byteBuffer.getShort();
        colorMapDepth = byteBuffer.get();
        xOrigin = byteBuffer.getShort();
        yOrigin = byteBuffer.getShort();
        width = byteBuffer.getShort();
        height = byteBuffer.getShort();
        bitsPerPixel = byteBuffer.get();
        imageDescriptor = byteBuffer.get();

        if (height < 0 || width  < 0 || bitsPerPixel < 1) {
            throw new RuntimeException("Invalid width, height or bits per pixel value.");
        }
    }

    public TGAHeader(short width, short height, byte bitsPerPixel, byte dataTypeCode, byte imageDescriptor) {
        this.width = width;
        this.height = height;
        this.bitsPerPixel = bitsPerPixel;
        this.dataTypeCode = dataTypeCode;
        this.imageDescriptor = imageDescriptor;
    }

    byte[] toByteArray() {
        byte[] bytes = new byte[18];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(idLength);
        byteBuffer.put(colorMapType);
        byteBuffer.put(dataTypeCode);
        byteBuffer.putShort(colorMapOrigin);
        byteBuffer.putShort(colorMapLength);
        byteBuffer.put(colorMapDepth);
        byteBuffer.putShort(xOrigin);
        byteBuffer.putShort(yOrigin);
        byteBuffer.putShort(width);
        byteBuffer.putShort(height);
        byteBuffer.put(bitsPerPixel);
        byteBuffer.put(imageDescriptor);
        return bytes;
    }

    @Override
    public String toString() {
        return "TGAHeader{" +
                "idLength=" + idLength +
                ", colorMapType=" + colorMapType +
                ", dataTypeCode=" + dataTypeCode +
                ", colorMapOrigin=" + colorMapOrigin +
                ", colorMapLength=" + colorMapLength +
                ", colorMapDepth=" + colorMapDepth +
                ", xOrigin=" + xOrigin +
                ", yOrigin=" + yOrigin +
                ", width=" + width +
                ", height=" + height +
                ", bitsPerPixel=" + bitsPerPixel +
                '}';
    }
}
