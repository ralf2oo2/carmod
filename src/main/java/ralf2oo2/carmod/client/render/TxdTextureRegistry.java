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

    public static void registerTexture(RenderwareBinaryStream.StructTextureData textureData){
        try{
            ByteBuffer textureBuffer;
            if(textureData.direct3dTextureFormat() != 0){
                int format = 0x44585431;
                DDSReader.Order order = DDSReader.DXT1Order;
                String textureFormat = new String(ByteBuffer.allocate(4).putInt((int)textureData.direct3dTextureFormat()).array(), StandardCharsets.UTF_8);
                switch (textureFormat){
                    case "1TXD": format = 0x44585431; order = DDSReader.DXT1Order; break;
                    case "3TXD": format = 0x44585433; order = DDSReader.DXT3Order; break;
                }
                int[] decodedTexture = DDSReader.read(textureData.data(), order, 0, textureData.width(), textureData.height(), format);
                textureBuffer = ByteBuffer.allocateDirect(textureData.width() * textureData.height() * 4);
                for(int pixel : decodedTexture){
                    textureBuffer.putInt(pixel);
                }
            }
            else{
                textureBuffer = ByteBuffer.allocateDirect(textureData.data().length);
                textureBuffer.put(textureData.data());
            }
            textureBuffer.rewind();

            int textureId = GL11.glGenTextures();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureData.width(), textureData.height(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

            TxdTexture txdTexture = new TxdTexture(textureId, textureData.textureName().replace("\0", ""), textureData.alphaName(), (int)textureData.version(), (int)textureData.filterFlags(), (int)textureData.direct3dTextureFormat(), textureData.width(), textureData.height(), (byte)textureData.depth(), (byte)textureData.mipmapCount(), (byte)textureData.texcodeType(), (byte)textureData.flags(), textureData.palette(), textureData.data(), textureData.mipmaps().toArray(new RenderwareBinaryStream.Mipmap[0]));
            txdTextures.add(txdTexture);
            System.out.println("Successfully added txd texture " + txdTexture.textureName);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}