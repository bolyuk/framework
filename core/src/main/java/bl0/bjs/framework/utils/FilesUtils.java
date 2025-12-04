package bl0.bjs.framework.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FilesUtils {

    public enum Type{
        File, Folder, All
    }

    public static String getJarLocation(Class<?> object) {
        try {
            var jarPath = new File(object
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getPath();
            return new File(jarPath).getParent();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @SneakyThrows
    public static void writeToFile(Path filePath, String data) {
            Files.write(filePath, data.getBytes());
            System.out.println("Data written to file: " + filePath);
    }

    @SneakyThrows
    public static String readFromFile(Path filePath){
        return Files.readString(filePath);
    }

    @SneakyThrows
    public static void delete(Path path) {
        Files.delete(path);
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    @SneakyThrows
    public static void makeDir(Path path) {
        Files.createDirectory(path);
    }

    public static boolean isFolder(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }

    public static boolean isFile(Path path) {
        return Files.exists(path) && Files.isRegularFile(path);
    }

    @SneakyThrows
    public static List<String> getEntities(Path folderPath, Type type) {
        List<String> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path path : stream) {
                boolean isFolder = Files.isDirectory(path);
                boolean isFile = Files.isRegularFile(path);

                if (type == Type.All ||
                        (type == Type.Folder && isFolder) ||
                        (type == Type.File && isFile)) {
                    result.add(path.toAbsolutePath().toString());
                }
            }
        }
        return result;
    }
}
