package crawler;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientUtil {

    public static int getStatus(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setInstanceFollowRedirects(true);
            con.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            int code = con.getResponseCode();
            return code;
        } catch (Exception e) {
            return 500; // treat network errors as server error / dead link
        }
    }
}
