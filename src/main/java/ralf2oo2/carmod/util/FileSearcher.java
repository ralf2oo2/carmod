package ralf2oo2.carmod.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileSearcher {
    public static List<File> getFilesByExtension(Path rootDir, String fileExtension) {
        List<File> result = new ArrayList<>();

        try {
            Files.walk(rootDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileExtension))
                    .map(Path::toFile)
                    .forEach(result::add);
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }

        return result;
    }
    public static List<File> getFilesByName(Path rootDir, String name) {
        List<File> result = new ArrayList<>();

        try {
            Files.walk(rootDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(name) ||
                            path.toString().equals(name))
                    .map(Path::toFile)
                    .forEach(result::add);
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }

        return result;
    }
    public static Optional<File> getFileByFilenameFromFileList(List<File> files, String fileName){
        for(int i = 0; i < files.size(); i++){
            String name = files.get(i).getName().replaceFirst("[.][^.]+$", "");
            if(name.equals(fileName)){
                return Optional.ofNullable(files.get(i));
            }
        }
        return Optional.empty();
    }
}
