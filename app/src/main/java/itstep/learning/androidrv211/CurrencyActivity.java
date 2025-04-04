package itstep.learning.androidrv211;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import itstep.learning.orm.CurrencyRateNbu;

public class CurrencyActivity extends AppCompatActivity {

    private static final String NBU_BASE_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    private RecyclerView recyclerView;
    private CurrencyAdapter currencyAdapter;
    private ExecutorService pool;
    private TextView tvDate;
    private EditText etSearch, etDate;
    private Spinner spinnerSort;
    private String currentSort = "Сортувати: A → Я";
    private final List<CurrencyRateNbu> allRates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        tvDate = findViewById(R.id.tvDate);
        etSearch = findViewById(R.id.etSearch);
        etDate = findViewById(R.id.etDate);
        spinnerSort = findViewById(R.id.spinnerSort);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyAdapter = new CurrencyAdapter();
        recyclerView.setAdapter(currencyAdapter);

        // Поиск
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRates(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Сортировка
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Сортувати: A → Я", "Сортувати: Курс ↑", "Сортувати: Курс ↓")
        );
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSort = parent.getItemAtPosition(position).toString();
                filterRates(etSearch.getText().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Календарь выбора даты
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String dateApi = String.format(Locale.US, "%04d%02d%02d", year, month + 1, dayOfMonth);
                        String dateDisplay = String.format(Locale.US, "%02d.%02d.%04d", dayOfMonth, month + 1, year);

                        etDate.setText(dateDisplay);
                        pool.submit(() -> loadRates(dateApi));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Загрузка курса на сегодня при входе
        String todayApi = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        String todayDisplay = new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(new Date());
        etDate.setText(todayDisplay);

        pool = Executors.newFixedThreadPool(3);
        pool.submit(() -> loadRates(todayApi));
    }

    private void loadRates(String date) {
        try {
            String urlStr = NBU_BASE_URL;
            if (date != null) {
                urlStr = NBU_BASE_URL + "&date=" + date;
            }

            URL url = new URL(urlStr);

            try (InputStream urlStream = url.openStream();
                 ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int len;
                while ((len = urlStream.read(buffer)) != -1) {
                    byteBuilder.write(buffer, 0, len);
                }

                String json = byteBuilder.toString(StandardCharsets.UTF_8.name());
                JSONArray jsonArray = new JSONArray(json);
                List<CurrencyRateNbu> rates = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    CurrencyRateNbu rate = CurrencyRateNbu.fromJsonObject(obj);
                    rates.add(rate);
                }

                runOnUiThread(() -> {
                    allRates.clear();
                    allRates.addAll(rates);
                    filterRates(etSearch.getText().toString());

                    if (!rates.isEmpty()) {
                        String dateStr = rates.get(0).getExchangedate();
                        tvDate.setText("Офіційний курс НБУ на " + dateStr);
                    }
                });

            }
        } catch (IOException | JSONException e) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Помилка: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private void filterRates(String query) {
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<CurrencyRateNbu> filtered = new ArrayList<>();

        for (CurrencyRateNbu rate : allRates) {
            if (rate.getTxt().toLowerCase(Locale.ROOT).contains(lowerQuery)
                    || rate.getCc().toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                filtered.add(rate);
            }
        }

        switch (currentSort.trim()) {
            case "Сортувати: Курс ↑":
                filtered.sort(Comparator.comparingDouble(CurrencyRateNbu::getRate));
                break;
            case "Сортувати: Курс ↓":
                filtered.sort((r1, r2) -> Double.compare(r2.getRate(), r1.getRate()));
                break;
            default:
                filtered.sort(Comparator.comparing(CurrencyRateNbu::getTxt, String.CASE_INSENSITIVE_ORDER));
                break;
        }

        currencyAdapter.submitList(filtered);
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