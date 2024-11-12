package ralf2oo2.carmod.util;

import org.lwjgl.util.Color;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.vehicle.data.CarCols;
import ralf2oo2.carmod.vehicle.data.VehicleColors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarColsReader {
    public CarCols getCarCols(String filePath){
        CarCols carCols = new CarCols();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean readingColSection = false;
            boolean readingCarSection = false;
            boolean readingCar4Section = false;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.equals("col")) {
                    readingColSection = true;
                    readingCarSection = false;
                    readingCar4Section = false;
                    continue;
                } else if (line.equals("car")) {
                    readingColSection = false;
                    readingCarSection = true;
                    readingCar4Section = false;
                    continue;
                } else if (line.equals("car4")) {
                    readingColSection = false;
                    readingCarSection = false;
                    readingCar4Section = true;
                } else if (line.equals("end")) {
                    readingColSection = false;
                    readingCarSection = false;
                    readingCar4Section = false;
                }

                String[] parts = line.split("#")[0].trim().split(",");
                if(readingColSection){
                    if(parts.length == 3){
                        try {
                            int red = Integer.parseInt(parts[0].trim());
                            int green = Integer.parseInt(parts[1].trim());
                            int blue = Integer.parseInt(parts[2].trim());
                            carCols.colors.add( new Color(red, green, blue));
                        } catch (NumberFormatException e) {
                            Carmod.logger.error("Skipping invalid color line: " + line + " : " + e.getMessage());
                        }
                    }
                }
                if(readingCarSection || readingCar4Section){
                    if(parts.length >= 3){
                        VehicleColors vehicleColors = new VehicleColors();
                        vehicleColors.name = parts[0].trim();

                        int[] indexes = new int[parts.length - 1];

                        try {
                            for (int i = 1; i < parts.length; i++) {
                                indexes[i - 1] = Integer.parseInt(parts[i].trim());
                            }
                        } catch (NumberFormatException e) {
                            Carmod.logger.error("Skipping invalid Carcol line: " + line  + " : " + e.getMessage());
                            continue;
                        }
                        vehicleColors.colIndexes = indexes;
                        if(readingCar4Section) vehicleColors.car4 = true;
                        carCols.vehicleColors.add(vehicleColors);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return carCols;
    }
}
