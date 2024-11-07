package ralf2oo2.carmod.mixin;

import net.minecraft.client.render.Culler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.CarmodClient;
import ralf2oo2.carmod.PhysicsEngine;
import ralf2oo2.carmod.Utils.Math;
import ralf2oo2.carmod.Utils.Util;
import ralf2oo2.carmod.client.render.DebugRenderer;
import ralf2oo2.carmod.entity.CarEntity;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow private int entityRenderCooldown;
    private final AtomicReference<Map<CarEntity, DBody[]>> collisionBodiesReference = new AtomicReference<>();
    private final AtomicReference<DRay> rayReference = new AtomicReference<>();
    private final AtomicReference<List<DVector3C>> hitPointsReference = new AtomicReference<>();

    @Inject(at = @At(value = "TAIL"), method = "renderEntities")
    private void carmod_handleMouseClick(Vec3d culler, Culler tickDelta, float par3, CallbackInfo ci) {
        if (entityRenderCooldown <= 0) {
            GL11.glDisable(GL11.GL_LIGHTING);
            PlayerEntity player = CarmodClient.getMc().player;
            Carmod.physicsEngine.executionQueue.add(() -> {
                collisionBodiesReference.set(Carmod.physicsEngine.getCollisionBodies());
                hitPointsReference.set(PhysicsEngine.hitPoints);
                if(Carmod.physicsEngine.ray != null){
                    rayReference.set((DRay) Carmod.physicsEngine.ray);
                }
            });
            if(collisionBodiesReference.get() != null){
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                GL11.glColor3f(1f, 1f, 1f);
                for (Map.Entry<CarEntity, DBody[]> entry : collisionBodiesReference.get().entrySet()) {
                    for(int i = 0; i < entry.getValue().length; i++){
                        Iterator<DGeom> iterator = entry.getValue()[i].getGeomIterator(); // Replace GeometryType with the actual type
                        while (iterator.hasNext()) {
                            DGeom geom = iterator.next();
                            Vec3d relativePos = Util.getRelativePos(player, geom.getPosition(), par3);
                            if(geom instanceof DSphere){
                                GL11.glPushMatrix();
                                GL11.glTranslatef((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
                                Util.applyMatrix(Math.convertMatrix(geom.getRotation().toFloatArray()));
                                DebugRenderer.renderSphere((float)((DSphere)geom).getRadius());
                                GL11.glPopMatrix();
                            }
                            if(geom instanceof DCylinder){
                                GL11.glPushMatrix();
                                GL11.glTranslatef((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
                                Util.applyMatrix(Math.convertMatrix(geom.getRotation().toFloatArray()));
                                DebugRenderer.renderCylinder((float)((DCylinder)geom).getRadius(), (float)((DCylinder)geom).getLength());
                                GL11.glPopMatrix();
                            }
                        }
                    }
                }
            }
            if(rayReference.get() != null){
                DRay ray = rayReference.get();
                GL11.glPushMatrix();
                Matrix4f matrix = Math.convertMatrix(ray.getRotation().toFloatArray());
                FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
                matrix.store(buffer);
                buffer.flip();
                Vec3d relativePos = Util.getRelativePos(player, ray.getPosition(), par3);
                GL11.glTranslatef((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
                GL11.glMultMatrix(buffer);

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                GL11.glColor3f(1f, 0f, 0f);
                GL11.glLineWidth(2.0f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3f(0f, 0f, 0f);
                GL11.glVertex3f(0f, 0f, (float)((DRay)ray).getLength());
                GL11.glEnd();
                GL11.glPopMatrix();
            }
            if(hitPointsReference.get() != null){
                List<DVector3C> hitPoints = hitPointsReference.get();
                for(int i = 0; i < hitPoints.size(); i++){
                    GL11.glPushMatrix();
                    GL11.glPointSize(5.0f);
                    GL11.glColor3f(0.0f, 0.0f, 1.0f);
                    Vec3d relativePos = Util.getRelativePos(player, hitPoints.get(i), par3);
                    GL11.glTranslatef((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
                    GL11.glBegin(GL11.GL_POINTS);
                    GL11.glVertex3f(0f, 0f, 0f);
                    GL11.glEnd();
                    GL11.glPopMatrix();
                }
            }
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }
}
