package ralf2oo2.carmod.client.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.tick.TickScheduler;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.CarmodClient;
import ralf2oo2.carmod.PhysicsEngine;
import ralf2oo2.carmod.Utils.Math;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CarEntityRenderer extends EntityRenderer {
    private CarRenderer carRenderer;
    private final AtomicReference<DBody[]> bodyReference = new AtomicReference<>();
    private final AtomicReference<DRay> rayReference = new AtomicReference<>();
    private final AtomicReference<List<DVector3C>> hitPointsReference = new AtomicReference<>();
    public CarEntityRenderer(){
        carRenderer = new CarRenderer();
    }
    @Override
    public void render(Entity entity, double x, double y, double z, float g, float h) {
        CarEntity carEntity = (CarEntity)entity;
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(carEntity.carName);
        if(vehicle.isPresent()){
            Carmod.physicsEngine.executionQueue.add(() -> {
                bodyReference.set(Carmod.physicsEngine.getEntityBodies(carEntity).get());
                hitPointsReference.set(PhysicsEngine.hitPoints);
                if(Carmod.physicsEngine.ray != null){
                    rayReference.set((DRay) Carmod.physicsEngine.ray);
                }
            });
            GL11.glPushMatrix();

            GL11.glTranslatef((float)x, (float)y, (float)z);

            if(carEntity.rotationMatrix != null){
                applyMatrix(carEntity.rotationMatrix);
            }

            if(!DebugRenderer.active){
                vehicle.get().vehicleModel.render(x, y, z, carEntity.getBrightnessAtEyes(h), ((Minecraft)FabricLoader.getInstance().getGameInstance()).player);
            }
            else {
                RenderwareBinaryStream.Col collisions = vehicle.get().vehicleCollisions.collisions;
                if(DebugRenderer.renderSpheres){
                    DebugRenderer.renderCollisionSpheres((float)x, (float)y, (float)z, collisions.spheres());
                }
                if(DebugRenderer.renderMesh){
                    DebugRenderer.renderFaces((float)x, (float)y, (float)z, collisions.faces(), collisions.vertices());
                }
                if(DebugRenderer.renderShadowMesh){
                    DebugRenderer.renderFaces((float)x, (float)y, (float)z, collisions.shadowfaces(), collisions.shadowvertices());
                }
                if(DebugRenderer.renderBounds){
                    DebugRenderer.renderBounds((float)x, (float)y, (float)z, collisions.header().bounds());
                }
            }
            GL11.glPopMatrix();

            renderWheels(vehicle.get(), carEntity, h);

            // TODO: move all this debug rendering to proper area
            if(false){
                if(bodyReference.get() == null) return;
                for(int i = 0; i < bodyReference.get().length; i++){
                    Iterator<DGeom> iterator = bodyReference.get()[i].getGeomIterator(); // Replace GeometryType with the actual type
                    while (iterator.hasNext()) {
                        DGeom geom = iterator.next();
                        DVector3 vector = (DVector3) geom.getPosition();
                        float relativePosX = (float)(vector.get0() - CarmodClient.getMc().player.x);
                        float relativePosY = (float)(vector.get1() - CarmodClient.getMc().player.y);
                        float relativePosZ = (float)(vector.get2() - CarmodClient.getMc().player.z);
                        if(geom instanceof DSphere){
                            GL11.glPushMatrix();
                            GL11.glTranslatef(relativePosX, relativePosY, relativePosZ);
                            applyMatrix(Math.convertMatrix(geom.getRotation().toFloatArray()));
                            DebugRenderer.renderSphere((float)((DSphere)geom).getRadius());
                            GL11.glPopMatrix();
                        }
                        if(geom instanceof DCylinder){
                            GL11.glPushMatrix();
                            GL11.glTranslatef(relativePosX, relativePosY, relativePosZ);
                            applyMatrix(Math.convertMatrix(geom.getRotation().toFloatArray()));
                            DebugRenderer.renderCylinder((float)((DCylinder)geom).getRadius(), (float)((DCylinder)geom).getLength());
                            GL11.glPopMatrix();
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
                PlayerEntity player = CarmodClient.getMc().player;
                DVector3C vector = ray.getPosition();
                GL11.glTranslatef((float)(vector.get0() - MathHelper.lerp(h, player.prevX, player.x)), (float)(vector.get1() - MathHelper.lerp(h, player.prevY, player.y)), (float)(vector.get2()- MathHelper.lerp(h, player.prevZ, player.z)));
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
                    DVector3C vector = hitPoints.get(i);
                    PlayerEntity player = CarmodClient.getMc().player;
                    GL11.glTranslatef((float)(vector.get0() - MathHelper.lerp(h, player.prevX, player.x)), (float)(vector.get1() - MathHelper.lerp(h, player.prevY, player.y)), (float)(vector.get2()- MathHelper.lerp(h, player.prevZ, player.z)));
                    GL11.glBegin(GL11.GL_POINTS);
                    GL11.glVertex3f(0f, 0f, 0f);
                    GL11.glEnd();
                    GL11.glPopMatrix();
                }
            }
        }
    }

    // TODO: move convertMatrix to util, this sucks
    private void applyMatrix(Matrix4f matrix){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.store(buffer);
        buffer.flip();
        GL11.glMultMatrix(buffer);
    }

    private void renderWheels(Vehicle vehicle, CarEntity carEntity, float delta){
        if(carEntity.wheelPositions == null || carEntity.wheelRotations == null) return;
        PlayerEntity player = ((Minecraft)FabricLoader.getInstance().getGameInstance()).player;
        for(int i = 0; i < carEntity.wheelPositions.size(); i++){
            GL11.glPushMatrix();
            DVector3C wheelPosition = carEntity.wheelPositions.get(i);
            GL11.glTranslatef((float)(wheelPosition.get0() - MathHelper.lerp(delta, player.prevX, player.x)), (float)(wheelPosition.get1() - MathHelper.lerp(delta, player.prevY, player.y)), (float)(wheelPosition.get2() - MathHelper.lerp(delta, player.prevZ, player.z)));

            applyMatrix(carEntity.wheelRotations.get(i));
            if(carEntity.wheelSides.get(i) == "left"){
                GL11.glRotatef(-90f, 0f, 1f, 0f);
            } else if(carEntity.wheelSides.get(i) == "right"){
                GL11.glRotatef(90f, 0f, 1f, 0f);
            }

            vehicle.vehicleModel.renderWheel(carEntity.getBrightnessAtEyes(delta), player);
            GL11.glPopMatrix();
        }
    }
}
