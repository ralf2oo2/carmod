package ralf2oo2.carmod.client.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.checkerframework.checker.units.qual.C;
import org.lwjgl.opengl.GL11;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DSphere;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.CarmodClient;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CarEntityRenderer extends EntityRenderer {
    private CarRenderer carRenderer;
    private final AtomicReference<DBody> bodyReference = new AtomicReference<>();
    public CarEntityRenderer(){
        carRenderer = new CarRenderer();
    }
    @Override
    public void render(Entity entity, double x, double y, double z, float g, float h) {
        CarEntity carEntity = (CarEntity)entity;
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(carEntity.carName);
        if(vehicle.isPresent()){
            GL11.glPushMatrix();

            GL11.glTranslatef((float)x, (float)y, (float)z);
            GL11.glTranslatef(0f, 1f, 0f);

            GL11.glRotatef(carEntity.vehiclePitch, 0.0f, 1.0f, 0.0f);

            GL11.glRotatef(carEntity.vehicleYaw - 90, 1.0f, 0.0f, 0.0f);

            GL11.glRotatef(carEntity.vehicleRoll, 0.0f, 0.0f, 1.0f);

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
            if(bodyReference.get() == null){
                Carmod.physicsEngine.executionQueue.add(() -> {
                    bodyReference.set(Carmod.physicsEngine.getEntityBody(carEntity).get());
                });
            }
            if(bodyReference.get() == null) return;
            Iterator<DGeom> iterator = bodyReference.get().getGeomIterator(); // Replace GeometryType with the actual type

                while (iterator.hasNext()) {
                    DGeom geom = iterator.next();
                    if(!(geom instanceof DSphere)) continue;
                    GL11.glPushMatrix();
                    DVector3 vector = (DVector3) geom.getPosition();
                    float relativePosX = (float)(vector.get0() - CarmodClient.getMc().player.x);
                    float relativePosY = (float)(vector.get1() - CarmodClient.getMc().player.y);
                    float relativePosZ = (float)(vector.get2() - CarmodClient.getMc().player.z);
                    GL11.glTranslatef(relativePosX, relativePosY, relativePosZ);

                    DebugRenderer.renderSphere((float)((DSphere)geom).getRadius());
                    //VehicleRegistry.getVehicle("test").get().vehicleModel.render(relativePosX, relativePosY, relativePosZ, 1f, CarmodClient.getMc().player);

                    GL11.glPopMatrix();
                }
        }
    }
}
