package ralf2oo2.carmod.client.render;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarRenderer {
    RenderwareBinaryStream geometryData;
    RenderwareBinaryStream textureData;
    private RenderwareBinaryStream.ListWithHeader frameList;
    private List<String> frameNames;
    private RenderwareBinaryStream.ListWithHeader geometryList;
    private List<RenderwareBinaryStream> atomicList;
    private List<RenderwareBinaryStream> textureList;

    private RenderwareBinaryStream.StructAtomic getStructAtomic(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructAtomic) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }

    private RenderwareBinaryStream.StructGeometry getStructGeometry(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructGeometry) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }

    private RenderwareBinaryStream.StructFrameList getStructFrameList(RenderwareBinaryStream.ListWithHeader stream){
        return (RenderwareBinaryStream.StructFrameList) stream.header();
    }

    private RenderwareBinaryStream.StructTextureData getStructTextureData(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructTextureData) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }

    private void applyFrameTransformations(RenderwareBinaryStream.Frame frame){
        if(frame.curFrameIdx() != 0){
            applyFrameTransformations(getStructFrameList(frameList).frames().get(frame.curFrameIdx()));
        }
        GL11.glTranslatef(frame.position().x(), frame.position().z(), -frame.position().y());
    }

    private void loadTextures(){
        for(int i = 0; i < textureList.size(); i++){
            RenderwareBinaryStream.StructTextureData textureData = getStructTextureData(textureList.get(i));
            TxdTextureRegistry.registerTexture(textureData);
        }
    }

    private void renderGeometry(int geometryIndex){
        try{
            GL11.glColor3f(1f, 1f, 1f);
            GL11.glEnable(GL11.GL_VERTEX_ARRAY);
            GL11.glPushMatrix();
            GL11.glRotatef(-90, 1f, 0f, 0f);

            RenderwareBinaryStream.StructGeometry geometry = getStructGeometry(geometryList.entries().get(geometryIndex));
            ArrayList<RenderwareBinaryStream.Vector3d> vertices = geometry.morphTargets().get(0).vertices();
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer((int)geometry.numTriangles() * 3 * 3);
            int offset = 0;
            for(RenderwareBinaryStream.Triangle triangle : geometry.geometry().triangles()){
                RenderwareBinaryStream.Vector3d vert1 = vertices.get(triangle.vertex1());
                RenderwareBinaryStream.Vector3d vert2 = vertices.get(triangle.vertex2());
                RenderwareBinaryStream.Vector3d vert3 = vertices.get(triangle.vertex3());

                float[] triangleVertices = new float[]{vert1.x(), vert1.y(), vert1.z(),
                        vert2.x(), vert2.y(), vert2.z(),
                        vert3.x(), vert3.y(), vert3.z()};
                vertexBuffer.put(triangleVertices, 0, triangleVertices.length);
            }
            vertexBuffer.flip();
            GL11.glVertexPointer(3, 0, vertexBuffer);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, (int)geometry.numTriangles() * 3);
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_VERTEX_ARRAY);
        }
        catch (Exception e){
        }
    }

    public void render(String path, double x, double y, double z){
        try{
            if(geometryData == null){
                this.geometryData = RenderwareBinaryStream.fromFile(path + ".dff");
                this.textureData = RenderwareBinaryStream.fromFile(path + ".txd");
                this.textureList = new ArrayList<>();
                ((RenderwareBinaryStream.ListWithHeader) textureData.body()).entries().forEach((entry) -> {
                    if(entry.code().name() == "TEXTURE_NATIVE"){
                        textureList.add(entry);
                    }
                });
                if(TxdTextureRegistry.textureCount() == 0){
                    loadTextures();
                }
                this.geometryList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().get(1).body();
                this.frameList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().get(0).body();
                this.atomicList = ((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().subList(3, ((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().size());

                RenderwareBinaryStream.ListWithHeader entries = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().get(0).body());
                for(RenderwareBinaryStream stream : entries.entries()){
                    byte[] array = (byte[])stream.body();
                    byte[] name = Arrays.copyOfRange(array, 12, array.length);
                    String namestr = new String(name, StandardCharsets.UTF_8);
                    System.out.println(namestr);
                    frameNames.add(namestr);
                }
            }
            int textureId = TxdTextureRegistry.getTextureId("lights");

            GL11.glTranslatef((float)x, (float)y, (float)z);
            for(int i = 0; i < atomicList.size(); i++){
                RenderwareBinaryStream.StructAtomic structAtomic = getStructAtomic(atomicList.get(i));
                RenderwareBinaryStream.Frame frame = getStructFrameList(frameList).frames().get((int)structAtomic.frameIndex());
                GL11.glPushMatrix();
                applyFrameTransformations(frame);
                renderGeometry((int)structAtomic.geometryIndex());
                GL11.glPopMatrix();
            }
//            System.out.println("pls");
//
//            Minecraft mc = Minecraft.class.cast(FabricLoader.getInstance().getGameInstance());
//
//            int screenWidth = mc.displayWidth;
//            int screenHeight = mc.displayHeight;
//            GL11.glMatrixMode(GL11.GL_PROJECTION);
//            GL11.glLoadIdentity();
//            GL11.glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
//
//            GL11.glEnable(GL11.GL_TEXTURE_2D);
//
//            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
//            float[] vertices = {
//                    0, 0, // bottom-left
//                    screenWidth, 0, // bottom-right
//                    screenWidth, screenHeight, // top-right
//                    0, screenHeight // top-left
//            };
//
//            float[] texCoords = {
//                    0, 1, // bottom-left
//                    1, 1, // bottom-right
//                    1, 0, // top-right
//                    0, 0 // top-left
//            };
//
//            GL11.glBegin(GL11.GL_QUADS);
//            for (int i = 0; i < 4; i++) {
//                GL11.glTexCoord2f(texCoords[i * 2], texCoords[i * 2 + 1]);
//                GL11.glVertex2f(vertices[i * 2], vertices[i * 2 + 1]);
//            }
//            GL11.glEnd();

        }
        catch (Exception e){
        }
    }
}
