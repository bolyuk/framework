package bl0.bjs.common.utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class HashUtil {
    public static String sha256(Path file) {
        try (InputStream is = Files.newInputStream(file)) {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[8192];

            int read;

            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            byte[] hash = digest.digest();

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
