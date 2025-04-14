package itstep.learning.androidrv211;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;
import android.util.Log;


public class Services {
public static String readAllText(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int len;
    while ((len = inputStream.read(buffer)) > 0) {
        byteBuilder.write(buffer, 0, len);
    }
    inputStream.close();
    return byteBuilder.toString(StandardCharsets.UTF_8.name());
}

        // Выполняет GET-запрос и возвращает содержимое
        public static String fetchUrl(String href) {
            try {
                URL url = new URL(href);
                InputStream urlStream = url.openStream();
                return readAllText(urlStream);
            } catch (MalformedURLException ex) {
                Log.e("Services::fetchUrl", "MalformedURLException: " + ex.getMessage());
            } catch (IOException ex) {
                Log.e("Services::fetchUrl", "IOException: " + ex.getMessage());
            }
            return null;
        }

        // Выполняет POST-запрос с author/msg и возвращает ответ сервера
        public static String sendMessageToServer(String chatUrl, String author, String message) {
            String charset = StandardCharsets.UTF_8.name();

            try {
                // Подготовка тела запроса
                String body = String.format(
                        Locale.ROOT,
                        "author=%s&msg=%s",
                        URLEncoder.encode(author, charset),
                        URLEncoder.encode(message, charset)
                );

                // Настройка подключения
                URL url = new URL(chatUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                // Заголовки
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty("X-Powered-By", "AndroidPv211");
                connection.setChunkedStreamingMode(0);

                // Отправка тела запроса
                OutputStream bodyStream = connection.getOutputStream();
                bodyStream.write(body.getBytes(charset));
                bodyStream.flush();
                bodyStream.close();

                int responseCode = connection.getResponseCode();
                Log.d("sendChatMessage", "Server response code: " + responseCode);

                if (responseCode == 201) {
                    // Успех, сервер не возвращает тело
                    return "";
                } else if (responseCode >= 200 && responseCode < 300) {
                    // Ответ с телом
                    InputStream inputStream = connection.getInputStream();
                    return readAllText(inputStream);
                } else {
                    // Ошибка от сервера
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        String error = readAllText(errorStream);
                        Log.e("sendChatMessage", "Error response: " + error);
                    }
                }

            } catch (UnsupportedEncodingException ex) {
                Log.e("sendChatMessage", "UnsupportedEncodingException: " + ex.getMessage());
            } catch (MalformedURLException ex) {
                Log.e("sendChatMessage", "MalformedURLException: " + ex.getMessage());
            } catch (IOException ex) {
                Log.e("sendChatMessage", "IOException: " + ex.getMessage());
            } catch (Exception ex) {
                Log.e("sendChatMessage", "Unexpected error: " + ex.getMessage());
            }

            return null;
        }
}