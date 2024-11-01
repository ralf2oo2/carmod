package ralf2oo2.carmod.client.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import ralf2oo2.carmod.entity.CarEntity;

public class CarEntityRenderer extends EntityRenderer {
    private CarRenderer carRenderer;
    public CarEntityRenderer(){
        carRenderer = new CarRenderer();
    }
    @Override
    public void render(Entity entity, double x, double y, double z, float g, float h) {
        CarEntity carEntity = (CarEntity)entity;
        CarModel carModel = CarModel.getCarModel(carEntity.carName);
        //System.out.println(((Minecraft)FabricLoader.getInstance().getGameInstance()).world.method_198(h));
        if(carModel != null){
            carModel.render(x, y, z, carEntity.getBrightnessAtEyes(h), ((Minecraft)FabricLoader.getInstance().getGameInstance()).player);
        }
        //System.out.println(carEntity.method_1394(h));
    }
}
