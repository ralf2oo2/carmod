package ralf2oo2.carmod.client.render;

import org.lwjgl.opengl.GL11;
import ralf2oo2.carmod.Utils.DDSReader;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TxdTextureRegistry {
    private static List<TxdTexture> txdTextures = new ArrayList<>();
    public static int textureCount(){
        return txdTextures.size();
    }

    public static List<TxdTexture> getTxdTextures(){
        return txdTextures;
    }

    public static int getTextureId(String textureName){
        List<TxdTexture> selectedTexture = txdTextures.stream().filter((texture) -> texture.textureName.equals(textureName)).collect(Collectors.toList());
        if(selectedTexture.stream().count() > 0 ){
            return selectedTexture.get(0).textureId;
        }
        return -1;
    }
//    public BufferedImage decompress(byte[] buffer, int width, int height) {
//        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        int[] scanline = new int[4 * width]; //stores 4 horizontal lines (width/4 blocks)
//
//        RGBA[] blockPalette = new RGBA[4]; //stores RGBA values of current block
//
//        int bufferOffset = 0;
//
//        for (int row = 0; row < height / 4; row++) {
//            for (int col = 0; col < width / 4; col++) {
//
//                short rgb0 = Short.reverseBytes(Bytes.getShort(buffer, bufferOffset));
//                short rgb1 = Short.reverseBytes(Bytes.getShort(buffer, bufferOffset + 2));
//                int bitmap = Integer.reverseBytes(Bytes.getInt(buffer, bufferOffset + 4));
//                bufferOffset += 8;
//
//                blockPalette[0] = R5G6B5.decode(rgb0);
//                blockPalette[1] = R5G6B5.decode(rgb1);
//
//                if ((rgb0 & 0xffff) <= (rgb1 & 0xffff)) {
//                    int c2r = (blockPalette[0].getRed() + blockPalette[1].getRed()) / 2;
//                    int c2g = (blockPalette[0].getGreen() + blockPalette[1].getGreen()) / 2;
//                    int c2b = (blockPalette[0].getBlue() + blockPalette[1].getBlue()) / 2;
//
//                    blockPalette[2] = new RGBA(c2r, c2g, c2b, 255);
//                    blockPalette[3] = new RGBA(0, 0, 0, 0);
//
//                } else {
//                    int c2r = (2 * blockPalette[0].getRed() + blockPalette[1].getRed()) / 3;
//                    int c2g = (2 * blockPalette[0].getGreen() + blockPalette[1].getGreen()) / 3;
//                    int c2b = (2 * blockPalette[0].getBlue() + blockPalette[1].getBlue()) / 3;
//
//                    int c3r = (blockPalette[0].getRed() + 2 * blockPalette[1].getRed()) / 3;
//                    int c3g = (blockPalette[0].getGreen() + 2 * blockPalette[1].getGreen()) / 3;
//                    int c3b = (blockPalette[0].getBlue() + 2 * blockPalette[1].getBlue()) / 3;
//
//
//                    blockPalette[2] = new RGBA(c2r, c2g, c2b, 255);
//                    blockPalette[3] = new RGBA(c3r, c3g, c3b, 255);
//
//                }
//
//                for (int i = 0; i < 16; i++, bitmap >>= 2) {
//                    int pi = (i / 4) * width + (col * 4 + i % 4);
//                    int index = bitmap & 3;
//                    //scanline[pi] = A8R8G8B8.encode(blockPalette[index]);
//                    scanline[pi] = A8R8G8B8.encode(blockPalette[index]);
//                }
//            }
//            //copy scanline to buffered image
//            result.setRGB(0, row * 4, width, 4, scanline, 0, width);
//        }
//        return result;
//    }

    public static void registerTexture(RenderwareBinaryStream.StructTextureData textureData){
        try{
//            ByteBuffer header = ByteBuffer.allocateDirect(124);
//            header.put("DDS ".getBytes(StandardCharsets.UTF_8));
//            header.putInt(124);
//            header.putInt(DdsHeader.Flags.DDSD_PIXELFORMAT.getAsInt());
//            header.putInt(textureData.height());
//            header.putInt(textureData.width());
//            header.putInt(0);
//            header.putInt(0);
//            header.putInt(0);
//            header.put(new byte[44]);
//            header.putInt(0x00000004);
//            header.putInt(0);
//            header.putInt(0);
//            header.putInt(0);
//            header.putInt(0);
//            header.putInt(0);
//
//            byte[] headerBytes = new byte[header.remaining()];
//            header.get(headerBytes);
//            byte[] decodedTex = DXT1Decoder.decodeDXT1FromFile(textureData.data(), textureData.width(), textureData.height());

            int format = 0x44585431;
            DDSReader.Order order = DDSReader.DXT1Order;
            String textureFormat = new String(ByteBuffer.allocate(4).putInt((int)textureData.direct3dTextureFormat()).array(), StandardCharsets.UTF_8);
            switch (textureFormat){
                case "1TXD": format = 0x44585431; order = DDSReader.DXT1Order; break;
                case "3TXD": format = 0x44585433; order = DDSReader.DXT3Order; break;
            }
            int[] decodedTexture = DDSReader.read(textureData.data(), order, 0, textureData.width(), textureData.height(), format);
            ByteBuffer textureBuffer = ByteBuffer.allocateDirect(textureData.width() * textureData.height() * 4);
            //ByteBuffer textureBuffer = ByteBuffer.allocateDirect(textureData.data().length + headerBytes.length);
            //textureBuffer.put(headerBytes, 0, headerBytes.length);
            for(int pixel : decodedTexture){
                textureBuffer.putInt(pixel);
            }
            textureBuffer.rewind();
//            Dds dds = new Dds();
//            dds.read(textureBuffer);

//            DdsImageDecoder decoder = new DdsImageDecoder();
//            byte[] png = decoder.convertToPNG(dds);

            int textureId = GL11.glGenTextures();

            //ByteBuffer uncompressedTextureBuffer = ByteBuffer.allocateDirect(png.length);
            //uncompressedTextureBuffer.put(png, 0, png.length);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureData.width(), textureData.height(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            TxdTexture txdTexture = new TxdTexture(textureId, textureData.textureName().replace("\0", ""), textureData.alphaName(), (int)textureData.version(), (int)textureData.filterFlags(), (int)textureData.direct3dTextureFormat(), textureData.width(), textureData.height(), (byte)textureData.depth(), (byte)textureData.mipmapCount(), (byte)textureData.texcodeType(), (byte)textureData.flags(), textureData.palette(), textureData.data(), textureData.mipmaps().toArray(new RenderwareBinaryStream.Mipmap[0]));
            txdTextures.add(txdTexture);
            System.out.println("Successfully added txd texture " + txdTexture.textureName);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}

//class RGBA{
//    public int r;
//    public int g;
//    public int b;
//    public int a;
//
//    public RGBA(int r, int g, int b, int a) {
//        this.r = r;
//        this.g = g;
//        this.b = b;
//        this.a = a;
//    }
//
//    public int getRed(){
//        return r;
//    }
//    public int getGreen(){
//        return g;
//    }
//    public int getBlue(){
//        return b;
//    }
//}
