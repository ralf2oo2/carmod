package ralf2oo2.carmod.util;

import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.vehicle.data.handling.VehicleHandling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HandlingReader {
    public List<VehicleHandling> getVehicleHandling(String filePath){
        List<VehicleHandling> vehicleHandlingList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("%") || line.startsWith("!") || line.startsWith("$") || line.startsWith("^")) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if(parts.length >= 35){
                    VehicleHandling vehicleHandling = new VehicleHandling();
                    try {
                        vehicleHandling.name = parts[0].trim();
                        vehicleHandling.mass = Float.parseFloat(parts[1].trim());
                        vehicleHandling.turnMass = Float.parseFloat(parts[2].trim());
                        vehicleHandling.drag = Float.parseFloat(parts[3].trim());
                        vehicleHandling.centerOfMassX = Float.parseFloat(parts[4].trim());
                        vehicleHandling.centerOfMassY = Float.parseFloat(parts[5].trim());
                        vehicleHandling.centerOfMassZ = Float.parseFloat(parts[6].trim());
                        vehicleHandling.percentSubmerged = Integer.parseInt(parts[7].trim());
                        vehicleHandling.tractionMultiplier = Float.parseFloat(parts[8].trim());
                        vehicleHandling.tractionLoss = Float.parseFloat(parts[9].trim());
                        vehicleHandling.tractionBias = Float.parseFloat(parts[10].trim());
                        vehicleHandling.numberOfGears = Integer.parseInt(parts[11].trim());
                        vehicleHandling.maxVelocity = Float.parseFloat(parts[12].trim());
                        vehicleHandling.engineAcceleration = Float.parseFloat(parts[13].trim());
                        vehicleHandling.engineInertia = Float.parseFloat(parts[14].trim());
                        vehicleHandling.driveType = parts[15].trim();
                        vehicleHandling.engineType = parts[16].trim();
                        vehicleHandling.brakeDeceleration = Float.parseFloat(parts[17].trim());
                        vehicleHandling.brakeBias = Float.parseFloat(parts[18].trim());
                        vehicleHandling.abs = Integer.parseInt(parts[19].trim());
                        vehicleHandling.steeringLock = Float.parseFloat(parts[20].trim());
                        vehicleHandling.suspensionForceLimit = Float.parseFloat(parts[21].trim());
                        vehicleHandling.suspensionDampeningLevel = Float.parseFloat(parts[22].trim());
                        vehicleHandling.suspensionHighSpdComDamp = Float.parseFloat(parts[23].trim());
                        vehicleHandling.suspensionUpperLimit = Float.parseFloat(parts[24].trim());
                        vehicleHandling.suspensionLowerLimit = Float.parseFloat(parts[25].trim());
                        vehicleHandling.suspensionBias = Float.parseFloat(parts[26].trim());
                        vehicleHandling.suspensionAntiDiveMultiplier = Float.parseFloat(parts[27].trim());
                        vehicleHandling.seatOffsetDistance = Float.parseFloat(parts[28].trim());
                        vehicleHandling.collisionDamageMultiplier = Float.parseFloat(parts[29].trim());
                        vehicleHandling.monetaryValue = Integer.parseInt(parts[30].trim());
                        vehicleHandling.modelFlags = (int)Long.parseLong(parts[31].trim(), 16);
                        vehicleHandling.handlingFlags = (int)Long.parseLong(parts[32].trim(), 16);
                        vehicleHandling.frontLights = Integer.parseInt(parts[33].trim());
                        vehicleHandling.rearLights = Integer.parseInt(parts[34].trim());
                        vehicleHandling.vehicleAnimationGroup = Integer.parseInt(parts[35].trim());

                        vehicleHandlingList.add(vehicleHandling);
                    } catch (NumberFormatException e) {
                        Carmod.logger.error("Skipping invalid Handling line: " + line + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vehicleHandlingList;
    }
}
