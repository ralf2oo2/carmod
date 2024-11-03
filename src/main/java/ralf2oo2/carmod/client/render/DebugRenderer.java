package ralf2oo2.carmod.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.util.ArrayList;

public class DebugRenderer {
    public static boolean active = false;

    public static void renderCollisionSpheres(float x, float y, float z, ArrayList<RenderwareBinaryStream.ColSphere> spheres){
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        for(int i = 0; i < spheres.size(); i++){
            RenderwareBinaryStream.ColSphere collisionSphere = spheres.get(i);
            GL11.glPushMatrix();
            GL11.glTranslatef(collisionSphere.center().x(), collisionSphere.center().y(), collisionSphere.center().z());
            Sphere sphere = new Sphere();
            sphere.setDrawStyle(GL11.GL_LINE);
            sphere.draw(collisionSphere.radius(), 32, 32);
            GL11.glPopMatrix();
        }
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }
}
