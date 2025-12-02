package bl0.bjs.framework.utils;

public class LinuxUtils {
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    public static boolean isSudo() {
        String sudoUid = System.getenv("SUDO_UID");
        if (sudoUid != null)
            return true;

        return System.getProperty("user.name").equals("root");
    }
}
