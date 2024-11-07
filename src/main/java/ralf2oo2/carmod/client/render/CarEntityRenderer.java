package ralf2oo2.carmod.client.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
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
import ralf2oo2.carmod.Utils.Util;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CarEntityRenderer extends EntityRenderer {
    @Override
    public void render(Entity entity, double x, double y, double z, float g, float h) {
        CarEntity carEntity = (CarEntity)entity;
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(carEntity.carName);
        if(vehicle.isPresent()){
            GL11.glPushMatrix();

            GL11.glTranslatef((float)x, (float)y, (float)z);

            if(carEntity.rotationMatrix != null){
                Util.applyMatrix(carEntity.rotationMatrix);
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
        }
    }

    private void renderWheels(Vehicle vehicle, CarEntity carEntity, float delta){
        if(carEntity.wheelPositions == null || carEntity.wheelRotations == null) return;
        PlayerEntity player = ((Minecraft)FabricLoader.getInstance().getGameInstance()).player;
        for(int i = 0; i < carEntity.wheelPositions.size(); i++){
            GL11.glPushMatrix();
            Vec3d relativePos = Util.getRelativePos(player, carEntity.wheelPositions.get(i), delta);
            GL11.glTranslatef((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);

            Util.applyMatrix(carEntity.wheelRotations.get(i));
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
