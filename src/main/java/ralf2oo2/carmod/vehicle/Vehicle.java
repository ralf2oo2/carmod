package ralf2oo2.carmod.vehicle;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import ralf2oo2.carmod.util.RenderwareBinaryStream;
import ralf2oo2.carmod.vehicle.data.CarCols;
import ralf2oo2.carmod.vehicle.data.VehicleData;
import ralf2oo2.carmod.vehicle.data.handling.VehicleHandling;

import java.io.IOException;

public class Vehicle {
    private RenderwareBinaryStream binaryStream;
    public VehicleData vehicleData;
    public VehicleHandling vehicleHandling;
    public CarCols carCols;
    public VehicleModel vehicleModel;
    public VehicleCollisions vehicleCollisions;
    public Vehicle(VehicleData vehicleData, VehicleHandling vehicleHandling, CarCols carCols, RenderwareBinaryStream binaryStream){
        this.vehicleData = vehicleData;
        this.vehicleHandling = vehicleHandling;
        this.carCols = carCols;
        this.binaryStream = binaryStream;
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            this.vehicleModel = new VehicleModel(binaryStream);
        }
        this.vehicleCollisions = new VehicleCollisions(binaryStream);
    }
}
