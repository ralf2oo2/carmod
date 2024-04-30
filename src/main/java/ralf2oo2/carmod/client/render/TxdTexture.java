package ralf2oo2.carmod.client.render;

import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

public class TxdTexture {
    public int textureId;
    public String textureName;
    public String alphaName;
    public int version;
    public int filterFlags;
    public int textureFormat;
    public int width;
    public int height;
    public byte depth;
    public byte mipmapCount;
    public byte texcodeType;
    public byte flags;
    public byte[] palette;
    public byte[] textureData;
    public RenderwareBinaryStream.Mipmap[] mipmaps;

    public TxdTexture(int textureId, String textureName, String alphaName, int version, int filterFlags, int textureFormat, int width, int height, byte depth, byte mipmapCount, byte texcodeType, byte flags, byte[] palette, byte[] textureData, RenderwareBinaryStream.Mipmap[] mipmaps) {
        this.textureId = textureId;
        this.textureName = textureName;
        this.alphaName = alphaName;
        this.version = version;
        this.filterFlags = filterFlags;
        this.textureFormat = textureFormat;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.mipmapCount = mipmapCount;
        this.texcodeType = texcodeType;
        this.flags = flags;
        this.palette = palette;
        this.textureData = textureData;
        this.mipmaps = mipmaps;
    }
}
