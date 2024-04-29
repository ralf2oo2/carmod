package ralf2oo2.carmod.client.render;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarRenderer {
    RenderwareBinaryStream data;
    private RenderwareBinaryStream.ListWithHeader frameList;
    private List<String> frameNames;
    private RenderwareBinaryStream.ListWithHeader geometryList;
    private List<RenderwareBinaryStream> atomicList;

    
    public void render(String path, double x, double y, double z){
        Tessellator tessellator = Tessellator.INSTANCE;
        try{
            if(data == null){
                this.data = RenderwareBinaryStream.fromFile(path);

                this.geometryList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) data.body()).entries().get(1).body();
                this.frameList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) data.body()).entries().get(0).body();
                this.atomicList = ((RenderwareBinaryStream.ListWithHeader) data.body()).entries().subList(3, ((RenderwareBinaryStream.ListWithHeader) data.body()).entries().size());

                RenderwareBinaryStream.ListWithHeader entries = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) data.body()).entries().get(0).body());
                for(RenderwareBinaryStream stream : entries.entries()){
                    byte[] array = (byte[])stream.body();
                    byte[] name = Arrays.copyOfRange(array, 12, array.length);
                    String namestr = new String(name, StandardCharsets.UTF_8);
                    System.out.println(namestr);
                    frameNames.add(namestr);
                }
            }
            //System.out.println(((RenderwareBinaryStream.ListWithHeader)data.body()).header());
//            GL11.glLoadIdentity();
            GL11.glColor3f(1f, 1f, 1f);
            GL11.glEnable(GL11.GL_VERTEX_ARRAY);
            for (RenderwareBinaryStream binaryStream : geometryList.entries() ) {

                GL11.glPushMatrix();
                GL11.glTranslatef((float)x, (float)y, (float)z);
                GL11.glRotatef(-90, 1f, 0f, 0f);

                RenderwareBinaryStream.StructGeometry geometry = (RenderwareBinaryStream.StructGeometry)((RenderwareBinaryStream.ListWithHeader)binaryStream.body()).header();
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
//                    tessellator.vertex(triangle.vertex1(), triangle.vertex2(), triangle.vertex3());
                    vertexBuffer.put(triangleVertices, 0, triangleVertices.length);
                }
                vertexBuffer.flip();
                GL11.glVertexPointer(3, 0, vertexBuffer);
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, (int)geometry.numTriangles() * 3);
                GL11.glPopMatrix();
            }
            GL11.glDisable(GL11.GL_VERTEX_ARRAY);
        }
        catch (Exception e){
        }
    }
}
