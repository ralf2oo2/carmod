package ralf2oo2.carmod.util;

import ralf2oo2.carmod.vehicle.data.VehicleData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDataReader {
    public List<VehicleData> getVehicles(String filePath){
        List<VehicleData> vehicles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 15) {
                    VehicleData vehicleData = new VehicleData();
                    vehicleData.id = Integer.parseInt(parts[0].trim());
                    vehicleData.dffName = parts[1].trim();
                    vehicleData.txdName = parts[2].trim();
                    vehicleData.category = parts[3].trim();
                    vehicleData.handlingId = parts[4].trim();
                    vehicleData.name = parts[5].trim();
                    vehicleData.animation = parts[6].trim().equals("null") ? null : parts[6].trim();
                    vehicleData.vClass = parts[7].trim();
                    vehicleData.frequency = Integer.parseInt(parts[8].trim());
                    vehicleData.flags = Integer.parseInt(parts[9].trim(), 16);
                    vehicleData.comprules = Integer.parseInt(parts[10].trim(), 16);
                    vehicleData.wheelModelId = Integer.parseInt(parts[11].trim());
                    vehicleData.frontWheelsSize = Float.parseFloat(parts[12].trim());
                    vehicleData.rearWheelsSize = Float.parseFloat(parts[13].trim());
                    vehicleData.tuningWheelType = Integer.parseInt(parts[14].trim());
                    vehicles.add(vehicleData);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vehicles;
    }
}
