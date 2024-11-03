package ralf2oo2.carmod.client.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.util.Optional;

public class CarEntityRenderer extends EntityRenderer {
    private CarRenderer carRenderer;
    public CarEntityRenderer(){
        carRenderer = new CarRenderer();
    }
    @Override
    public void render(Entity entity, double x, double y, double z, float g, float h) {
        CarEntity carEntity = (CarEntity)entity;
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(carEntity.carName);
        if(vehicle.isPresent()){
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
        }

    }
}
