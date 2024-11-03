package ralf2oo2.carmod.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleRegistry {
    public static List<Vehicle> vehicles = new ArrayList<>();

    @EventListener
    public void registerVehicles(TextureRegisterEvent event){
        String path = FabricLoader.getInstance().getConfigDir() + "/";
        LoadVehicle(path, "test");
    }

    public void LoadVehicle(String path, String name){
        vehicles.add(new Vehicle(path, name));
    }

    public static Optional<Vehicle> getVehicle(String name){
        if(name == null || name.isEmpty()){
            return Optional.empty();
        }
         return vehicles.stream().findAny().filter((vehicle) -> vehicle.name.equals(name));
    }
}
