package ralf2oo2.carmod.vehicle;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import ralf2oo2.carmod.util.RenderwareBinaryStream;
import ralf2oo2.carmod.vehicle.data.VehicleData;

import java.io.IOException;

public class Vehicle {
    private RenderwareBinaryStream binaryStream;
    public VehicleData vehicleData;
    public VehicleModel vehicleModel;
    public VehicleCollisions vehicleCollisions;
    public Vehicle(VehicleData vehicleData, RenderwareBinaryStream binaryStream){
        this.vehicleData = vehicleData;
        this.binaryStream = binaryStream;
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            this.vehicleModel = new VehicleModel(binaryStream);
        }
        this.vehicleCollisions = new VehicleCollisions(binaryStream);
    }
}
