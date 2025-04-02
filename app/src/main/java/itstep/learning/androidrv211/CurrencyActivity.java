package itstep.learning.androidrv211;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import itstep.learning.orm.CurrencyRateNbu;

public class CurrencyActivity extends AppCompatActivity {
    private static final String nbuRatesUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    private RecyclerView recyclerView;
    private CurrencyAdapter currencyAdapter;
    private ExecutorService pool;
    private TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        // Настройка отступов под системные панели
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация Views
        tvDate = findViewById(R.id.tvDate);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyAdapter = new CurrencyAdapter(new ArrayList<>());
        recyclerView.setAdapter(currencyAdapter);

        pool = Executors.newFixedThreadPool(3);
        pool.submit(this::loadRates);
    }

    private void loadRates() {
        try {
            URL url = new URL(nbuRatesUrl);

            try (InputStream urlStream = url.openStream();
                 ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int len;
                while ((len = urlStream.read(buffer)) != -1) {
                    byteBuilder.write(buffer, 0, len);
                }

                String json = byteBuilder.toString(StandardCharsets.UTF_8.name());

                try {
                    JSONArray jsonArray = new JSONArray(json);
                    List<CurrencyRateNbu> rates = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        CurrencyRateNbu rate = CurrencyRateNbu.fromJsonObject(obj);
                        rates.add(rate);
                    }

                    // Обновление UI
                    runOnUiThread(() -> {
                        currencyAdapter.updateData(rates);

                        if (!rates.isEmpty()) {
                            String date = rates.get(0).getExchangedate();
                            tvDate.setText("Офіційний курс НБУ на " + date);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(CurrencyActivity.this, "Ошибка парсинга: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        } catch (MalformedURLException e) {
            Log.d("loadRates", "MalformedURLException " + e.getMessage());
        } catch (IOException e) {
            Log.d("loadRates", "IOException " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(CurrencyActivity.this, "Помилка з'єднання: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
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