package com.github.kurtulusarkan.softwarerenderer.tga;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by kurtulusarkan on 1/22/17.
 */
public class TGAImage {

    private static final int MAX_CHUNK_LENGTH = 128;

    byte[] data;

    int width;
    int height;
    int bytesPerPixel;

    private TGAImage(TGAHeader header, ByteBuffer byteBuffer) {

        this.width = header.width;
        this.height = header.height;
        this.bytesPerPixel = header.bitsPerPixel >> 3; // bit to byte
        this.data = new byte[width * height * bytesPerPixel];

        if (bytesPerPixel != TGAHeader.BBP_GRAYSCALE &&
                bytesPerPixel != TGAHeader.BBP_RGB &&
                bytesPerPixel != TGAHeader.BBP_RGBA) {
            throw new RuntimeException("Invalid byte per pixel value. (unsupported.)");
        }

        switch (header.dataTypeCode) {
            case TGAHeader.DTC_UNCOMPRESSED_TRUECOLOR:
            case TGAHeader.DTC_UNCOMPRESSED_GRAYSCALE:
                byteBuffer.rewind();
                byteBuffer.position(18); // 18 bytes of header.
                byteBuffer.get(this.data);
                break;

            case TGAHeader.DTC_RLE_TRUECOLOR:
            case TGAHeader.DTC_RLE_GRAYSCALE:
                byteBuffer.rewind();
                byteBuffer.position(18);
                decodeRLE(byteBuffer);
                break;

            default:
                throw new RuntimeException("Unsupported file format. (i.e. compressed/color-mapped tga files.)");
        }
    }

    private void decodeRLE(ByteBuffer byteBuffer) {

        long pixelCount = width * height;

        long currentPixel = 0;
        int currentByte = 0;

        do {
            int chunkHeader = byteBuffer.get() & 0xFF;

            if (chunkHeader < MAX_CHUNK_LENGTH) {
                chunkHeader++;

                for (int i = 0; i < chunkHeader; i++) {
                    for (int t = 0; t < bytesPerPixel; t++) {
                        data[currentByte++] = byteBuffer.get();
                    }
                    currentPixel++;
                }
            } else {
                chunkHeader -= 127;
                byte[] colorBuffer = new byte[bytesPerPixel];
                byteBuffer.get(colorBuffer);

                for (int i = 0; i < chunkHeader; i++) {
                    for (int t = 0; t < bytesPerPixel; t++) {
                        data[currentByte++] = colorBuffer[t];
                    }
                    currentPixel++;
                }
            }
        } while (currentPixel < pixelCount);
    }

    public static TGAImage readTGAImageFromFile(final String fileName) throws IOException {

        Path path = Paths.get(fileName);
        byte[] bytes = Files.readAllBytes(path);

        return readTGAImageFromByteArray(bytes);
    }

    public static TGAImage readTGAImageFromByteArray(final byte[] bytes) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        TGAHeader header = new TGAHeader(byteBuffer);
        TGAImage image = new TGAImage(header, byteBuffer);

        return image;
    }

    public void writeToFile(final String fileName, boolean runLengthEncoded) throws IOException {

        byte[] developerAreaRef = new byte[4];
        byte[] extensionAreaRef = new byte[4];
        byte[] footer = {'T','R','U','E','V','I','S','I','O','N','-','X','F','I','L','E','.','\0'};

        byte dataTypeCode = bytesPerPixel == TGAHeader.BBP_GRAYSCALE ?
                (runLengthEncoded ? TGAHeader.DTC_RLE_GRAYSCALE : TGAHeader.DTC_UNCOMPRESSED_GRAYSCALE) :
                (runLengthEncoded ? TGAHeader.DTC_RLE_TRUECOLOR : TGAHeader.DTC_UNCOMPRESSED_TRUECOLOR);

        byte bitsPerPixel = (byte)(bytesPerPixel << 3);
        byte imageDescriptor = 0x20;

        TGAHeader header = new TGAHeader((short)width, (short)height, bitsPerPixel, dataTypeCode, imageDescriptor);

        FileOutputStream fileOut = new FileOutputStream(fileName);

        fileOut.write(header.toByteArray());

        if (!runLengthEncoded) {
            fileOut.write(data);
        } else {
            writeRunLenghtEncoded(fileOut);
        }

        fileOut.write(developerAreaRef);
        fileOut.write(extensionAreaRef);
        fileOut.write(footer);

        fileOut.close();
    }

    private void writeRunLenghtEncoded(FileOutputStream fileOut) throws IOException {

        int pixelCount = width * height;
        int currentPixel = 0;

        while (currentPixel < pixelCount) {

            int chunkStart = currentPixel * bytesPerPixel;
            int currentByte = currentPixel * bytesPerPixel;
            int runLength = 1;

            boolean raw = true;

            while (currentPixel + runLength < pixelCount && runLength < MAX_CHUNK_LENGTH) {

                boolean successEqual = true;
                for (int t = 0; successEqual && t < bytesPerPixel; t++) {
                    successEqual = (data[currentByte + t] == data[currentByte + t + bytesPerPixel]);
                }

                currentByte += bytesPerPixel;

                if (1 == runLength) {
                    raw = !successEqual;
                }
                if (raw && successEqual) {
                    runLength--;
                    break;
                }
                if (!raw && !successEqual) {
                    break;
                }

                runLength++;
                currentPixel += runLength;

                fileOut.write(raw ? runLength - 1 : runLength + 127);
                fileOut.write(Arrays.copyOfRange(data, chunkStart, chunkStart + (raw ? (runLength*bytesPerPixel):bytesPerPixel)));
            }
        }
    }

    @Override
    public String toString() {
        return "TGAImage{" +
                "width=" + width +
                ", height=" + height +
                ", bytesPerPixel=" + bytesPerPixel +
                '}';
    }
}
