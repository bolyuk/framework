package bl0.bjs.common.utils;

public final class MemoryFormatter {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;
    private static final long TB = GB * 1024;

    public static String format(long bytes) {
        if (bytes >= TB) return String.format("%.2f TB", bytes / (double) TB);
        if (bytes >= GB) return String.format("%.2f GB", bytes / (double) GB);
        if (bytes >= MB) return String.format("%.2f MB", bytes / (double) MB);
        if (bytes >= KB) return String.format("%.2f KB", bytes / (double) KB);
        return bytes + " B";
    }
}
