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

    public TGAImage(int width, int height, int bytesPerPixel) {
        this.width = width;
        this.height = height;
        this.bytesPerPixel = bytesPerPixel;
        this.data = new byte[width * height * bytesPerPixel];
    }

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

        if (!((header.imageDescriptor & 0x20) > 0)) {
            flipVertically();
        }
        if ((header.imageDescriptor & 0x10) > 0) {
            flipHorizontally();
        }
    }

    public TGAColor get(int x, int y) {
        if (data == null || x < 0 || y < 0 || x >= width || y >= height) {
            return new TGAColor();
        }
        int begin = (x + y * width) * bytesPerPixel;
        int end = begin + bytesPerPixel;
        byte[] color = Arrays.copyOfRange(data, begin, end);
        return new TGAColor(color, bytesPerPixel);
    }

    public void set(int x, int y, TGAColor color) {
        if (data == null || x < 0 || y < 0 || x >= width || y >= height)
            return;
        int begin = (x + y * width) * bytesPerPixel;
        System.arraycopy(color.color, 0, data, begin, bytesPerPixel);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBytesPerPixel() {
        return bytesPerPixel;
    }

    public void flipHorizontally() {

        int half = width >> 1;
        for (int i = 0; i < half; i++) {
            for (int j = 0; j < height; j++) {
                TGAColor c1 = get(i, j);
                TGAColor c2 = get(width - 1 - i, j);
                set(i, j, c2);
                set(width - 1 - i, j, c1);
            }
        }
    }

    public void flipVertically() {

        int bytesPerLine = width * bytesPerPixel;
        byte[] line = new byte[bytesPerLine];
        int half = height >> 1;
        for (int j = 0; j < half; j++) {
            int l1 = j * bytesPerLine;
            int l2 = (height - 1 - j) * bytesPerLine;
            System.arraycopy(data, l1, line, 0, bytesPerLine);
            System.arraycopy(data, l2, data, l1, bytesPerLine);
            System.arraycopy(line, 0, data, l2, bytesPerLine);
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
        byte[] footer = {'T', 'R', 'U', 'E', 'V', 'I', 'S', 'I', 'O', 'N', '-', 'X', 'F', 'I', 'L', 'E', '.', '\0'};

        byte dataTypeCode = bytesPerPixel == TGAHeader.BBP_GRAYSCALE ?
                (runLengthEncoded ? TGAHeader.DTC_RLE_GRAYSCALE : TGAHeader.DTC_UNCOMPRESSED_GRAYSCALE) :
                (runLengthEncoded ? TGAHeader.DTC_RLE_TRUECOLOR : TGAHeader.DTC_UNCOMPRESSED_TRUECOLOR);

        byte bitsPerPixel = (byte) (bytesPerPixel << 3);
        byte imageDescriptor = 0x20;

        TGAHeader header = new TGAHeader((short) width, (short) height, bitsPerPixel, dataTypeCode, imageDescriptor);

        FileOutputStream fileOut = new FileOutputStream(fileName);

        fileOut.write(header.toByteArray());

        if (!runLengthEncoded) {
            fileOut.write(data);
        } else {
            writeRunLengthEncoded(fileOut);
        }

        fileOut.write(developerAreaRef);
        fileOut.write(extensionAreaRef);
        fileOut.write(footer);

        fileOut.close();
    }

    private void writeRunLengthEncoded(FileOutputStream fileOut) throws IOException {

        int pixelCount = width * height;
        int currentPixel = 0;

        while (currentPixel < pixelCount) {

            int chunkStart = currentPixel * bytesPerPixel;
            int currentByte = currentPixel * bytesPerPixel;
            int runLength = 1;
            boolean raw = true;

            while (currentPixel + runLength < pixelCount && runLength < MAX_CHUNK_LENGTH) {

                boolean equalPixel = true;
                for (int t = 0; t < bytesPerPixel; t++) {
                    equalPixel = (data[currentByte + t] == data[currentByte + t + bytesPerPixel]);
                    if (!equalPixel) {
                        break;
                    }
                }

                currentByte += bytesPerPixel;

                if (1 == runLength) {
                    raw = !equalPixel;
                }
                if (raw && equalPixel) {
                    runLength--;
                    break;
                }
                if (!raw && !equalPixel) {
                    break;
                }

                runLength++;
            }

            currentPixel += runLength;
            int chunkHeader = raw ? (runLength - 1) : (runLength + 127);
            int bytesToCopy = raw ? (runLength * bytesPerPixel) : bytesPerPixel;
            fileOut.write(chunkHeader);
            fileOut.write(Arrays.copyOfRange(data, chunkStart, chunkStart + bytesToCopy));
        }
    }

    public void scale(int newWidth, int newHeight) {

        if (newHeight < 0 || newWidth < 0)
            return;

        byte[] newData = new byte[newHeight * newWidth * bytesPerPixel];

        int nScanLine = 0;
        int oScanLine = 0;
        int errY = 0;

        int nLineBytes = newWidth * bytesPerPixel;
        int oLineBytes = width * bytesPerPixel;

        for (int j = 0; j < height; j++) {
            int errx = width - newWidth;
            int nx = -bytesPerPixel;
            int ox = -bytesPerPixel;
            for (int i = 0; i < width; i++) {
                ox += bytesPerPixel;
                errx += newWidth;
                while (errx >= width) {
                    errx -= width;
                    nx += bytesPerPixel;
                    System.arraycopy(data, oScanLine + ox, newData, nScanLine + nx, bytesPerPixel);
                }
            }
            errY += newHeight;
            oScanLine += oLineBytes;
            while (errY >= height) {
                if (errY >= height << 1) { // it means we jump over a scanline
                    System.arraycopy(newData, nScanLine, newData, nScanLine + nLineBytes, nLineBytes);
                }
                errY -= height;
                nScanLine += nLineBytes;
            }
        }

        data = newData;
        width = newWidth;
        height = newHeight;
    }

    @Override
    public String toString() {
        return "TGAImage{" +
                "width=" + width +
                ", height=" + height +
                ", bytesPerPixel=" + bytesPerPixel +
                '}';
    }

    public void drawLine(int x1, int y1, int x2, int y2, TGAColor color) {
        boolean steep = false;
        if (Math.abs(x1 - x2) < Math.abs(y1 - y2)) {
            int t = x1;
            x1 = y1;
            y1 = t;
            //Math.swap(x1, y1);
            t = x2;
            x2 = y2;
            y2 = t;
            //Math.swap(x2, y2);
            steep = true;
        }
        if (x1 > x2) {
            int t = x1;
            x1 = x2;
            x2 = t;
            //Math.swap(x1, x2);
            t = y1;
            y1 = y2;
            y2 = t;
            //Math.swap(y1, y2);
        }
        int dx = x2 - x1;
        int dy = y2 - y1;
        int dError2 = Math.abs(dy) * 2;
        int error2 = 0;
        int y = y1;
        for (int x = x1; x <= x2; x++) {
            if (steep) {
                set(y, x, color);
            } else {
                set(x, y, color);
            }
            error2 += dError2;
            if (error2 > dx) {
                y += (y2 > y1 ? 1 : -1);
                error2 -= dx * 2;
            }
        }
    }
}
