package ralf2oo2.carmod.vehicle;

import org.lwjgl.BufferUtils;
import org.ode4j.ode.DTriMeshData;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import ralf2oo2.carmod.util.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class VehicleCollisions {
    private RenderwareBinaryStream binaryStream;
    public RenderwareBinaryStream.Col collisions;
    public DTriMeshData meshCollider;
    public VehicleCollisions(RenderwareBinaryStream binaryStream){
        this.binaryStream = binaryStream;
        RenderwareBinaryStream.StructExtension colextension = (RenderwareBinaryStream.StructExtension) ((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().get(((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().size() - 1).body();
        this.collisions = ((RenderwareBinaryStream.CollisionExtension)colextension.extension()).col();
        generateMeshCollider(collisions.faces(), collisions.vertices());
    }

    // TODO: maybe put the data directly in an array instead of using buffers;
    private void generateMeshCollider(ArrayList<RenderwareBinaryStream.ColFace> faces, ArrayList<RenderwareBinaryStream.ColVertex> vertices){
        DTriMeshData meshData = OdeHelper.createTriMeshData();

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size() * 3);
        for (RenderwareBinaryStream.ColVertex vertex : vertices) {
            ArrayList<Short> vertexCoords = vertex.vertex();

            if (vertexCoords.size() >= 3) {
                vertexBuffer.put(vertexCoords.get(0) / 128.0f); // x
                vertexBuffer.put(vertexCoords.get(2) / 128.0f); // y
                vertexBuffer.put(vertexCoords.get(1) / 128.0f); // z
            }
        }
        vertexBuffer.flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(faces.size() * 3);
        for(RenderwareBinaryStream.ColFace face : faces){
            indexBuffer.put(face.a());
            indexBuffer.put(face.b());
            indexBuffer.put(face.c());
        }
        indexBuffer.flip();

        float[] vertexArray = new float[vertexBuffer.remaining()];
        int[] indexArray = new int[indexBuffer.remaining()];

        vertexBuffer.get(vertexArray);
        indexBuffer.get(indexArray);

        meshData.build(vertexArray, indexArray);
        meshData.preprocess();
        this.meshCollider = meshData;
    }

    public void InstanceiateCollisions(DWorld world){

    }
}
