package ralf2oo2.carmod.client.render;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Struct;
import java.util.*;

class Vertex {
    static int SIZE = Float.SIZE * 6;
    float x, y, z; // Position
    float uOffset, vOffset; // UV offsets
    float r, g, b, a;
    float materialIndex;
}

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

    private List<Material> getMaterialListForGeometry(int geometryIndex){
        List<Material> materials = new ArrayList<>();

        RenderwareBinaryStream.ListWithHeader materialList = (RenderwareBinaryStream.ListWithHeader) ((RenderwareBinaryStream.ListWithHeader)geometryList.entries().get(geometryIndex).body()).entries().get(0).body();
        RenderwareBinaryStream.StructMaterialList structMaterialList = (RenderwareBinaryStream.StructMaterialList)materialList.header();
        for(int i = 0; i < structMaterialList.materialCount(); i++){
            RenderwareBinaryStream.ListWithHeader material = (RenderwareBinaryStream.ListWithHeader) materialList.entries().get(i).body();
            RenderwareBinaryStream.StructMaterial structMaterial = (RenderwareBinaryStream.StructMaterial)material.header();
            Rgba rgba = new Rgba(structMaterial.color().r(), structMaterial.color().g(), structMaterial.color().b(), structMaterial.color().a());
            if(structMaterial.isTextured() > 0){
                RenderwareBinaryStream.ListWithHeader texture = (RenderwareBinaryStream.ListWithHeader) material.entries().get(0).body();
                RenderwareBinaryStream.StructTexture structTexture = (RenderwareBinaryStream.StructTexture) texture.header();
                RenderwareBinaryStream.StructString textureName = (RenderwareBinaryStream.StructString)texture.entries().get(0).body();
                RenderwareBinaryStream.StructString maskName = (RenderwareBinaryStream.StructString)texture.entries().get(1).body();
                materials.add(new Material((int)structMaterial.flags(), rgba, structMaterial.ambient(), structMaterial.specular(), structMaterial.diffuse(), (int)structTexture.filterFlags(), textureName.value(), maskName.value()));
                continue;
            }
            materials.add(new Material((int)structMaterial.flags(), rgba, structMaterial.ambient(), structMaterial.specular(), structMaterial.diffuse()));
        }
        return  materials;
    }

    private void renderGeometry(int geometryIndex){
        //GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glPushMatrix();
        try{
            List<Material> materials = getMaterialListForGeometry(geometryIndex);
            GL11.glRotatef(-90, 1f, 0f, 0f);

            RenderwareBinaryStream.StructGeometry geometry = getStructGeometry(geometryList.entries().get(geometryIndex));
            ArrayList<RenderwareBinaryStream.Vector3d> vertices = geometry.morphTargets().get(0).vertices();
            RenderwareBinaryStream.UvLayer uvLayer = geometry.geometry().uvLayers().get(0);
            //FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer((int)geometry.numTriangles() * 3 * 3);
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer((int)geometry.numVertices() * Vertex.SIZE);
            List<int[]> materialForTriangleList = new ArrayList<>();
            Vertex[] vertexes = new Vertex[(int)geometry.numVertices()];
            int triangles = 0;
            int currentMaterial = -1;
            int vertexOffset = 0;
            for(RenderwareBinaryStream.Triangle triangle : geometry.geometry().triangles()){
                Vertex vertex1 = new Vertex();
                Vertex vertex2 = new Vertex();
                Vertex vertex3 = new Vertex();
                if(currentMaterial == -1){
                    currentMaterial = triangle.materialId();
                }
                if(currentMaterial != triangle.materialId()){
                    materialForTriangleList.add(new int[]{triangles, currentMaterial});
                    triangles = 0;
                    currentMaterial = triangle.materialId();
                }
                //System.out.println(triangle.materialId());
                RenderwareBinaryStream.Vector3d vert1 = vertices.get(triangle.vertex1());
                RenderwareBinaryStream.Vector3d vert2 = vertices.get(triangle.vertex2());
                RenderwareBinaryStream.Vector3d vert3 = vertices.get(triangle.vertex3());

                vertex1.x = vert1.x();
                vertex1.y = vert1.y();
                vertex1.z = vert1.z();
                vertex2.x = vert2.x();
                vertex2.y = vert2.y();
                vertex2.z = vert2.z();
                vertex3.x = vert3.x();
                vertex3.y = vert3.y();
                vertex3.z = vert3.z();

                vertex1.uOffset = uvLayer.texCoords().get(vertexOffset).u();
                vertex1.vOffset = uvLayer.texCoords().get(vertexOffset).v();
                vertex2.uOffset = uvLayer.texCoords().get(vertexOffset + 1).u();
                vertex2.vOffset = uvLayer.texCoords().get(vertexOffset + 1).v();
                vertex3.uOffset = uvLayer.texCoords().get(vertexOffset + 2).u();
                vertex3.vOffset = uvLayer.texCoords().get(vertexOffset + 2).v();

                vertex1.materialIndex = currentMaterial;
                vertex2.materialIndex = currentMaterial;
                vertex3.materialIndex = currentMaterial;

//                float[] triangleVertices = new float[]{vert1.x(), vert1.y(), vert1.z(),
//                        vert2.x(), vert2.y(), vert2.z(),
//                        vert3.x(), vert3.y(), vert3.z()};
                //vertexBuffer.put(triangleVertices, 0, triangleVertices.length);
                vertexes[vertexOffset] = vertex1;
                vertexes[vertexOffset + 1] = vertex2;
                vertexes[vertexOffset + 2] = vertex3;
                triangles++;
                vertexOffset += 3;
            }

            if(triangles > 0){
                materialForTriangleList.add(new int[]{triangles, currentMaterial});
            }
            for(Vertex vertex : vertexes){
                vertexBuffer.put(vertex.x).put(vertex.y).put(vertex.z);
                vertexBuffer.put(vertex.uOffset).put(vertex.vOffset);
                vertexBuffer.put(vertex.materialIndex);
            }

            vertexBuffer.flip();

            int vbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

            int stride = 6 * Float.BYTES; // 3 position + 2 UV offset
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0); // Position
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES); // UV offset
            GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, stride, 5 * Float.BYTES); // UV offset

            GL20.glEnableVertexAttribArray(0); // Position
            GL20.glEnableVertexAttribArray(1); // UV offset
            GL20.glEnableVertexAttribArray(2); // UV offset

            //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexes.length);

            GL20.glDisableVertexAttribArray(0); // Position
            GL20.glDisableVertexAttribArray(1); // UV offset
            GL20.glDisableVertexAttribArray(2); // UV offset

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL15.glDeleteBuffers(vbo);

//            for(int[] materialforTriangle : materialForTriangleList){
//                Material mat = materials.get(materialforTriangle[1]);
//                GL11.glVertexPointer(3, 0, vertexBuffer);
//                GL11.glColor4f((float)mat.color.r / 255f, (float)mat.color.g / 255f, (float)mat.color.b / 255f, (float)mat.color.a / 255f);
//                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, (int)materialforTriangle[0] * 3);
//            }
        }
        catch (Exception e){
        }
        GL11.glPopMatrix();
        //GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
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

            GL11.glTranslatef((float)x, (float)y, (float)z);
            for(int i = 0; i < atomicList.size(); i++){
                RenderwareBinaryStream.StructAtomic structAtomic = getStructAtomic(atomicList.get(i));
                RenderwareBinaryStream.Frame frame = getStructFrameList(frameList).frames().get((int)structAtomic.frameIndex());
                GL11.glPushMatrix();
                applyFrameTransformations(frame);
                renderGeometry((int)structAtomic.geometryIndex());
                GL11.glPopMatrix();
            }

        }
        catch (Exception e){
        }
    }
}
