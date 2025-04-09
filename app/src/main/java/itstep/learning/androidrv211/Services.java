package itstep.learning.androidrv211;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Services {
    public static String fetchUrl(String href) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(href);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    )
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }



}