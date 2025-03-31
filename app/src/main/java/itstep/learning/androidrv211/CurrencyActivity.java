package itstep.learning.androidrv211;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyActivity extends AppCompatActivity {
    private static final String nbuRatesUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private TextView tvContainer;
    private ExecutorService pool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvContainer = findViewById(R.id.text_rates);
        pool = Executors.newFixedThreadPool(3);
        pool.submit(this::loadRates);
    }

    private void loadRates() {
        try {
            URL url = new URL(nbuRatesUrl);
            InputStream urlStream = url.openStream();
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;

            while ((len = urlStream.read(buffer)) > 0) {
                byteBuilder.write(buffer, 0, len);
            }

            String json = byteBuilder.toString(StandardCharsets.UTF_8.name());
            urlStream.close();

            Gson gson = new Gson();
            TypeToken<List<CurrencyRateNbu>> token = new TypeToken<List<CurrencyRateNbu>>() {};
            List<CurrencyRateNbu> rates = gson.fromJson(json, token.getType());

            // 🔠 Сортировка по алфавиту (по txt)
            rates.sort(Comparator.comparing(rate -> rate.txt));

            // 🧾 Форматированный вывод
            StringBuilder sb = new StringBuilder();
            for (CurrencyRateNbu rate : rates) {
                sb.append("💱 ")
                        .append(rate.txt)
                        .append(" (").append(rate.cc).append("): ")
                        .append(String.format("%.2f грн", rate.rate))
                        .append(" 📅 ").append(rate.exchangedate)
                        .append("\n\n");
            }

            runOnUiThread(() -> tvContainer.setText(sb.toString()));

        } catch (MalformedURLException ex) {
            Log.d("loadRates", "MalformedURLException " + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadRates", "IOException " + ex.getMessage());
            runOnUiThread(() -> tvContainer.setText("Помилка з'єднання: " + ex.getMessage()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pool.shutdown();
    }
}
/*
 ================================
 📡 Мережева робота в Android
 ================================

🔸 Android НЕ дозволяє працювати з мережею у головному (UI) потоці —
це викликає помилку:
❌ android.os.NetworkOnMainThreadException

📌 Рішення:
Потрібно виконувати мережеві запити у фонових потоках:
- new Thread(() -> ...)
- ExecutorService (ThreadPool)
- Coroutine (у Kotlin)
- або бібліотеки: Retrofit, Volley тощо.

--------------------------------------------------
🔒 Дозволи (Permissions)
--------------------------------------------------
Деякі дії в Android вимагають дозволів (permissions), наприклад — доступ до Інтернету.

❗ Помилка:
java.lang.SecurityException: Permission denied

📌 Рішення:
В AndroidManifest.xml потрібно додати:
<uses-permission android:name="android.permission.INTERNET" />

--------------------------------------------------
⚠️ Інша поширена помилка:
--------------------------------------------------
android.view.ViewRootImpl$CalledFromWrongThreadException

❗ Причина:
Спроба змінити UI (наприклад, TextView.setText(...)) з фонового потоку.

📌 Рішення:
Оновлювати UI тільки в головному потоці:

runOnUiThread(() -> {
    myTextView.setText("Результат");
});

--------------------------------------------------
🧠 Підсумок:
--------------------------------------------------
1️⃣ Усі мережеві запити — лише в окремому потоці!
2️⃣ Доступ до UI — тільки через runOnUiThread()
3️⃣ Не забувай дозволи в AndroidManifest.xml
4️⃣ Краще використовувати ExecutorService або Retrofit/OkHttp
*/