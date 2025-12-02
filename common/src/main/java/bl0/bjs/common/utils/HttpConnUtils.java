package bl0.bjs.common.utils;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HttpConnUtils {
    public static boolean checkIfActive(URI uri)
    {
        try {
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(300);
            conn.setReadTimeout(300);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200)
                return false;

        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
}
