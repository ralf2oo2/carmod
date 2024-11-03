package ralf2oo2.carmod.Utils;

public class BinaryStreamHelpers {
    public static RenderwareBinaryStream.StructAtomic getStructAtomic(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructAtomic) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }

    public static RenderwareBinaryStream.StructFrameList getStructFrameList(RenderwareBinaryStream.ListWithHeader stream){
        return (RenderwareBinaryStream.StructFrameList) stream.header();
    }

    public static RenderwareBinaryStream.StructTextureData getStructTextureData(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructTextureData) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }
}
