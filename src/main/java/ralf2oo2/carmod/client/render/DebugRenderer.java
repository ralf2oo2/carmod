package ralf2oo2.carmod.client.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class DebugRenderer {
    public static boolean active = false;
    public static boolean renderSpheres = true;
    public static boolean renderMesh = true;
    public static boolean renderBounds = true;
    public static boolean renderShadowMesh = false;

    public static void renderCollisionSpheres(float x, float y, float z, ArrayList<RenderwareBinaryStream.ColSphere> spheres){
        GL11.glPushMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        for(int i = 0; i < spheres.size(); i++){
            RenderwareBinaryStream.ColSphere collisionSphere = spheres.get(i);
            GL11.glPushMatrix();
            GL11.glTranslatef(collisionSphere.center().x(), collisionSphere.center().z(), collisionSphere.center().y());
            Sphere sphere = new Sphere();
            sphere.setDrawStyle(GLU.GLU_LINE);
            sphere.draw(collisionSphere.radius(), 16, 16);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    public static void renderSphere(float radius){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        Sphere sphere = new Sphere();
        sphere.setDrawStyle(GLU.GLU_LINE);
        sphere.draw(radius, 16, 16);
    }

    public static void renderFaces(float x, float y, float z, ArrayList<RenderwareBinaryStream.ColFace> faces, ArrayList<RenderwareBinaryStream.ColVertex> vertices){
        GL11.glPushMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
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

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glVertexPointer(3, 0, vertexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indexBuffer);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glPopMatrix();
    }
    public static void renderBounds(float x, float y, float z, RenderwareBinaryStream.ColBounds bounds){
        GL11.glPushMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        RenderwareBinaryStream.ColVec3 min = bounds.min();
        RenderwareBinaryStream.ColVec3 max = bounds.max();
        RenderwareBinaryStream.ColVec3 center = bounds.center();
        GL11.glTranslatef(center.x(), center.y(), center.z());
        GL11.glBegin(GL11.GL_LINES);

        // Front face
        addLine(min.x(), min.z(), min.y(), max.x(), min.z(), min.y()); // Bottom front
        addLine(max.x(), min.z(), min.y(), max.x(), max.z(), min.y()); // Right front
        addLine(max.x(), max.z(), min.y(), min.x(), max.z(), min.y()); // Top front
        addLine(min.x(), max.z(), min.y(), min.x(), min.z(), min.y()); // Left front

        // Back face
        addLine(min.x(), min.z(), max.y(), max.x(), min.z(), max.y()); // Bottom back
        addLine(max.x(), min.z(), max.y(), max.x(), max.z(), max.y()); // Right back
        addLine(max.x(), max.z(), max.y(), min.x(), max.z(), max.y()); // Top back
        addLine(min.x(), max.z(), max.y(), min.x(), min.z(), max.y()); // Left back

        // Connecting lines between front and back faces
        addLine(min.x(), min.z(), min.y(), min.x(), min.z(), max.y()); // Left
        addLine(max.x(), min.z(), min.y(), max.x(), min.z(), max.y()); // Right
        addLine(max.x(), max.z(), min.y(), max.x(), max.z(), max.y()); // Top
        addLine(min.x(), max.z(), min.y(), min.x(), max.z(), max.y()); // Bottom

        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private static void addLine(float x1, float y1, float z1, float x2, float y2, float z2) {
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y2, z2);
    }
}
