package bl0.bjs.common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {
    public static Path getJarFolderPath(Class<?> object) {
       return Paths.get(PathUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    }

    public static Path getAppDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        Path dir;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");

            if (appData == null || appData.isBlank())
                throw new RuntimeException("APPDATA environment variable is missing");

            dir = Paths.get(appData);

        } else {
            String xdg = System.getenv("XDG_DATA_HOME");

            if (xdg != null && !xdg.isBlank())
                dir = Paths.get(xdg);
             else
                dir = Paths.get(System.getProperty("user.home"),".local","share");
        }

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException( "Failed to create app data directory", e);
        }

        return dir;
    }
}
