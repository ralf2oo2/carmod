package ralf2oo2.carmod.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.physics.Car;
import ralf2oo2.carmod.util.*;
import ralf2oo2.carmod.vehicle.Vehicle;
import ralf2oo2.carmod.vehicle.data.CarCols;
import ralf2oo2.carmod.vehicle.data.VehicleData;
import ralf2oo2.carmod.vehicle.data.handling.VehicleHandling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleRegistry {
    private final VehicleDataReader vehicleDataReader = new VehicleDataReader();
    private final CarColsReader carColsReader = new CarColsReader();
    private final HandlingReader handlingReader = new HandlingReader();
    private List<File> dffFiles = new ArrayList<>();
    private List<File> txdFiles = new ArrayList<>();
    private List<VehicleData> vehicleData = new ArrayList<>();
    private List<VehicleHandling> vehicleHandling = new ArrayList<>();
    public static CarCols carCols;
    public static List<Vehicle> vehicles = new ArrayList<>();

    @EventListener
    public void registerVehicles(TextureRegisterEvent event){
        String path = FabricLoader.getInstance().getConfigDir() + "/carmod/";
        File configDir = new File(path);
        if(!configDir.exists()){
            configDir.mkdirs();
            Carmod.logger.info("Successfully created config dir");
        }

        dffFiles = FileSearcher.getFilesByExtension(new File(path).toPath(), "dff");
        txdFiles = FileSearcher.getFilesByExtension(new File(path).toPath(), "txd");
        loadVehicleData(path);
        loadVehicleHandling(path);
        loadCarCols(path);
        registerVehicles();
        System.out.println(vehicleData);
    }

    private void loadVehicleHandling(String path){
        vehicleHandling = handlingReader.getVehicleHandling(path + "handling.cfg");
    }

    private void loadVehicleData(String path){
        List<File> vehicleDataFiles = FileSearcher.getFilesByName(new File(path).toPath(), "vehicles.ide");
        vehicleDataFiles.forEach((vehicleDataFile) -> {
            vehicleData.addAll(vehicleDataReader.getVehicles(vehicleDataFile.getPath()));
        });
    }

    public void loadCarCols(String path){
        File file = new File(path + "carcols.dat");
        if(!file.exists()){
            Carmod.logger.info("carcols.dat not found, skipping...");
        }
        else{
            carCols = carColsReader.getCarCols(file.getPath());
        }
    }

    private void registerVehicles(){
        vehicleData.forEach(this::registerVehicle);
    }

    public void registerVehicle(VehicleData vehicleData){
        Optional<File> dffFile = FileSearcher.getFileByFilenameFromFileList(dffFiles, vehicleData.dffName);
        Optional<File> txdFile = FileSearcher.getFileByFilenameFromFileList(txdFiles, vehicleData.txdName);
        if(!dffFile.isPresent()){
            Carmod.logger.warn("Dff file not found: " + vehicleData.dffName);
            return;
        }
        if(!txdFile.isPresent()){
            Carmod.logger.warn("Txd file not found: " + vehicleData.txdName);
        }
        else {
            try {
                RenderwareBinaryStream txdStream = RenderwareBinaryStream.fromFile(txdFile.get().getPath());
                VehicleTextureRegistry.loadTextureDictionary(txdStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            RenderwareBinaryStream dffStream = RenderwareBinaryStream.fromFile(dffFile.get().getPath());
            vehicles.add(new Vehicle(vehicleData, dffStream));
        } catch (IOException e) {
            Carmod.logger.error("Failed to add vehicle: " + vehicleData.name);
        }
    }

    public static Optional<Vehicle> getVehicle(String name){
        if(name == null || name.isEmpty()){
            return Optional.empty();
        }
         return vehicles.stream().filter((vehicle) -> vehicle.vehicleData.name.equals(name)).findAny();
    }
}
