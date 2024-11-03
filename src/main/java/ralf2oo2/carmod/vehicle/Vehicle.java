package ralf2oo2.carmod.vehicle;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.io.IOException;
import java.util.Optional;

public class Vehicle {
    private RenderwareBinaryStream binaryStream;
    public VehicleModel vehicleModel;
    public VehicleCollisions vehicleCollisions;
    public String name;
    public Vehicle(RenderwareBinaryStream binaryStream, String name){
        this.binaryStream = binaryStream;
        this.name = name;
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            this.vehicleModel = new VehicleModel(binaryStream);
        }
        this.vehicleCollisions = new VehicleCollisions(binaryStream);
    }
    public Vehicle(String path, String name){
        try {
            this.binaryStream = RenderwareBinaryStream.fromFile(path + name + ".dff");
            this.name = name;
            if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
                this.vehicleModel = new VehicleModel(binaryStream);
            }
            this.vehicleCollisions = new VehicleCollisions(binaryStream);
        } catch (IOException e) {
            System.out.println("Failed to load model " + name + "dff");
        }
    }
}
