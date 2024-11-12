package ralf2oo2.carmod.util;

import org.lwjgl.util.Color;
import ralf2oo2.carmod.vehicle.data.CarCols;
import ralf2oo2.carmod.vehicle.data.VehicleColors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
                            System.err.println("Skipping invalid color line: " + line);
                        }
                    }
                }
                if(readingCarSection || readingCar4Section){
                    if(parts.length >= 3){
                        VehicleColors vehicleColors = new VehicleColors();
                        vehicleColors.name = parts[0].trim();
                        // TODO: load color indexes in array
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return carCols;
    }
}
