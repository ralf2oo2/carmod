package ralf2oo2.carmod.util;

import java.nio.ByteBuffer;

public class DXT1Decoder {
    public static byte[] decodeDXT1FromFile(byte[] imageData, int width, int height) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(imageData.length * 4);
        try {
            int blockSize = 8;
            int numBlocksX = (width + 3) / 4;
            int numBlocksY = (height + 3) / 4;

            ByteBuffer buffer = ByteBuffer.wrap(imageData);

            for (int blockY = 0; blockY < numBlocksY; blockY++) {
                for (int blockX = 0; blockX < numBlocksX; blockX++) {
                    decodeDXT1Block(buffer, blockX * blockSize, blockY * blockSize, outputBuffer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] result = new byte[outputBuffer.remaining()];
        outputBuffer.put(result);
        return result;
    }

    private static void decodeDXT1Block(ByteBuffer buffer, int x, int y, ByteBuffer outputBuffer) {
        short color0 = buffer.getShort();
        short color1 = buffer.getShort();
        int colorTable = buffer.getInt();

        int[] colors = new int[4];
        colors[0] = unpack565(color0);
        colors[1] = unpack565(color1);

        if (color0 > color1) {
            colors[2] = (2 * colors[0] + colors[1] + 1) / 3;
            colors[3] = (colors[0] + 2 * colors[1] + 1) / 3;
        } else {
            colors[2] = (colors[0] + colors[1]) / 2;
            colors[3] = 0;
        }

        for (int py = 0; py < 4; py++) {
            for (int px = 0; px < 4; px++) {
                int index = (colorTable >> 2 * (4 * py + px)) & 0x03;
                int color = colors[index];
                outputBuffer.putInt(color);
                // Now you can use the color value for the pixel at (x + px, y + py)
                // This is where you would normally write this color to your output buffer or display it
            }
        }
    }

    private static int unpack565(short c) {
        int r = (c & 0xF800) >> 8;
        int g = (c & 0x07E0) >> 3;
        int b = (c & 0x001F) << 3;
        return (r << 16) | (g << 8) | b;
    }
}
