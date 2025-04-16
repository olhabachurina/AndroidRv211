package itstep.learning.androidrv211;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import itstep.chat.ChatMessageAdapter;
import itstep.learning.orm.ChatMessage;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private static final String DB_NAME = "chat_db";
    private static final String AUTHOR_FILE_NAME = "author.name";

    private static final String TABLE_AUTHORS = "authors";
    private static final String TABLE_HISTORY = "chat_history";
    private final List<ChatMessage> messages = new ArrayList<>();
    private String currentUser = "";

    private ChatMessage messageToEdit = null;
    private TextView chatStatus;
    private RecyclerView recyclerView;
    private EditText etAuthor, etMessage;
    private CheckBox cbRememberMe;
    private MediaPlayer newMessageSound;
    private ImageView bellIcon;
    private Animation bellAnimation;
    private Button btnSend, btnClearAuthor;
    private ChatMessageAdapter adapter;
    private ExecutorService pool;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler autoUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initDatabase();
        loadMessagesFromDbAsync();
        debugPrintAuthorsFromDb();
        debugPrintMessagesFromDb();
        initViews();
        initRecyclerView();
        initSendButton();

        updateRunnable = () -> {
            updateChat();
            autoUpdateHandler.postDelayed(updateRunnable, 30000);
            Log.d("ChatActivity", "🔄 Автоматичне оновлення заплановано");
        };

        updateChat();
        autoUpdateHandler.postDelayed(updateRunnable, 30000);
    }
    private void debugPrintAuthorsFromDb() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT name FROM " + TABLE_AUTHORS, null);
        Log.d("ChatActivity", "🔍 Збережені імена авторів:");
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            Log.d("ChatActivity", "➡ " + name);
        }
        cursor.close();
        db.close();
    }
    private void debugPrintMessagesFromDb() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT id, author, text, moment FROM " + TABLE_HISTORY, null);
        Log.d("ChatActivity", "🧾 Історія повідомлень:");
        while (cursor.moveToNext()) {
            Log.d("ChatActivity", "🗨️ " +
                    cursor.getString(0) + " | " +
                    cursor.getString(1) + ": " +
                    cursor.getString(2) + " @ " +
                    cursor.getString(3));
        }
        cursor.close();
        db.close();
    }
    private void initDatabase() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AUTHORS + " (name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (id TEXT, author TEXT, text TEXT, moment TEXT)");
        db.close();
        Log.i("ChatActivity", "📂 База даних ініціалізована");
    }

    private void insertMessageIntoDb(ChatMessage msg) {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        ContentValues values = new ContentValues();
        values.put("id", msg.getId());
        values.put("author", msg.getAuthor());
        values.put("text", msg.getText());
        values.put("moment", ChatMessage.dateFormat.format(msg.getMoment()));

        long result = db.insert(TABLE_HISTORY, null, values);
        printAllMessagesFromDb();
        db.close();

        if (result == -1) {
            Log.e("ChatActivity", "❌ Помилка при збереженні повідомлення в БД: " + msg.toString());
        } else {
            Log.d("ChatActivity", "✅ Повідомлення збережено в БД з ID: " + result + ", вміст: " + msg);
        }
    }

    private void loadMessagesFromDbAsync() {
        CompletableFuture.runAsync(() -> {
            SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("SELECT id, author, text, moment FROM " + TABLE_HISTORY, null);

            final int[] count = {0};  // ⬅ обгортаємо в масив

            synchronized (messages) {
                while (cursor.moveToNext()) {
                    try {
                        String id = cursor.getString(0);
                        if (messages.stream().noneMatch(msg -> msg.getId().equals(id))) {
                            ChatMessage msg = new ChatMessage(
                                    id,
                                    cursor.getString(1),
                                    cursor.getString(2),
                                    ChatMessage.dateFormat.parse(cursor.getString(3))
                            );
                            messages.add(msg);
                            count[0]++;
                        }
                    } catch (ParseException e) {
                        Log.e("ChatActivity", "❌ Неможливо розпарсити дату з БД", e);
                    }
                }
                messages.sort(Comparator.comparing(ChatMessage::getMoment));
            }

            cursor.close();
            db.close();

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                Log.i("ChatActivity", "📥 Завантажено " + count[0] + " нових повідомлень з БД (асинхронно)");
            });
        });
    }

    private void saveAuthorToDb(String authorName) {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABLE_AUTHORS);
        ContentValues values = new ContentValues();
        values.put("name", authorName);
        db.insert(TABLE_AUTHORS, null, values);
        db.close();
        Log.i("ChatActivity", "✅ Ім’я автора збережено в БД: " + authorName);
    }

    private String loadAuthorFromDb() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT name FROM " + TABLE_AUTHORS + " LIMIT 1", null);
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        Log.i("ChatActivity", name != null ? "📥 Ім’я завантажено з БД: " + name : "ℹ️ Ім’я автора не знайдено в БД");
        return name;
    }

    private void initViews() {
        chatStatus = findViewById(R.id.chat_status);
        recyclerView = findViewById(R.id.recycler_messages);
        etAuthor = findViewById(R.id.et_author);
        etMessage = findViewById(R.id.et_message);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        bellIcon = findViewById(R.id.bell_icon);
        btnSend = findViewById(R.id.btn_send);
        btnClearAuthor = findViewById(R.id.btn_clear_author);

        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bell);
        newMessageSound = MediaPlayer.create(this, R.raw.bell_sound);

        // Завантаження збереженого імені автора
        String savedAuthor = loadAuthorFromFile();
        if (savedAuthor != null) {
            etAuthor.setText(savedAuthor);
            etAuthor.setEnabled(false);
            cbRememberMe.setChecked(true);
            currentUser = savedAuthor;
            Log.i("ChatActivity", "✅ Ім’я автора завантажено з файлу: " + savedAuthor);
        } else {
            Log.i("ChatActivity", "ℹ️ Файл автора відсутній, ім’я не встановлено.");
        }

        btnClearAuthor.setOnClickListener(v -> {
            deleteFile(AUTHOR_FILE_NAME);
            etAuthor.setEnabled(true);
            etAuthor.setText("");
            currentUser = "";
            cbRememberMe.setChecked(false);
            showStatus("🧹 Ім’я очищено з пам’яті", android.R.color.darker_gray);
            Log.i("ChatActivity", "🧹 Ім’я автора очищено з файлу");
        });

        findViewById(R.id.btn_debug_add).setOnClickListener(v -> generateTestMessages());
        findViewById(R.id.btn_exit).setOnClickListener(v -> finish());
        findViewById(R.id.btn_show_db_history).setOnClickListener(v -> {
            printAllMessagesFromDb();
        });
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(messages, currentUser);
        configureAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void initSendButton() {
        btnSend.setOnClickListener(v -> {
            String author = etAuthor.getText().toString().trim();
            String text = etMessage.getText().toString().trim();

            if (author.isEmpty()) {
                etAuthor.setError(getString(R.string.error_empty_author));
                return;
            }
            if (text.isEmpty()) {
                etMessage.setError(getString(R.string.error_empty_message));
                return;
            }

            Log.d("ChatActivity", "⏩ Натиснута кнопка надсилання");

            if (messageToEdit != null) {
                messageToEdit.setText(text);
                adapter.notifyDataSetChanged();
                etMessage.setText("");
                messageToEdit = null;
                btnSend.setText("Надіслати");
                showStatus("✏️ Повідомлення оновлено", android.R.color.holo_blue_dark);
                Log.d("ChatActivity", "✏️ Оновлено повідомлення: " + text);
                return;
            }

            if (!author.equals(currentUser)) {
                currentUser = author;
                adapter = new ChatMessageAdapter(messages, currentUser);
                configureAdapter();
                recyclerView.setAdapter(adapter);
                etAuthor.setEnabled(false);

                // Збереження імені автора, якщо встановлено чекбокс
                if (cbRememberMe.isChecked()) {
                    saveAuthorToFile(author);
                } else {
                    Log.i("ChatActivity", "📌 Ім’я не збережено — чекбокс не активний.");
                }
            }

            ChatMessage message = new ChatMessage("0", author, text, new Date());
            adapter.addMessage(message);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            playNewMessageSound();
            Log.d("ChatActivity", "📤 Додано повідомлення: " + text);

            CompletableFuture.runAsync(() -> {
                String response = Services.sendMessageToServer(CHAT_URL, author, text);
                Log.d("ChatActivity", "📡 Відповідь сервера: " + response);

                if (response == null || response.trim().isEmpty()) {
                    runOnUiThread(() -> showStatus(getString(R.string.error_network), android.R.color.holo_red_dark));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response);
                    if (json.optInt("status", 0) == 1) {
                        runOnUiThread(() -> {
                            showStatus(getString(R.string.status_message_sent), android.R.color.holo_green_dark);
                            etMessage.setText("");
                            updateChat();
                        });
                    } else {
                        runOnUiThread(() -> showStatus("❌ Статус сервера: " + json.optInt("status"), android.R.color.holo_red_dark));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> showStatus("❌ Помилка обробки JSON: " + e.getMessage(), android.R.color.holo_red_dark));
                    Log.e("ChatActivity", "❌ Помилка розбору JSON", e);
                }
            });
        });
    }

    private void configureAdapter() {
        adapter.setOnItemClickListener(message -> {
            etMessage.setText(message.getText());
            messageToEdit = message;
            btnSend.setText("Оновити");
            Log.d("ChatActivity", "🖊️ Обране повідомлення для редагування: " + message.getText());
        });

        adapter.setOnItemLongClickListener(message -> {
            messages.remove(message);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            showStatus("🗑️ Повідомлення видалено", android.R.color.holo_red_dark);
            Log.d("ChatActivity", "🗑️ Видалено повідомлення: " + message.getText());
        });
    }

    private void updateChat() {
        Log.i("ChatActivity", "🔄 Оновлення чату...");
        handler.postDelayed(() -> {
            pool = Executors.newFixedThreadPool(3);
            CompletableFuture
                    .supplyAsync(() -> Services.fetchUrl(CHAT_URL), pool)
                    .thenApply(this::parseChatResponse)
                    .thenAccept(this::processChatResponse);
        }, 2000);
    }
    private void printAllMessagesFromDb() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT id, author, text, moment FROM " + TABLE_HISTORY, null);
        Log.d("ChatActivity", "📚 Повна історія повідомлень з БД:");
        while (cursor.moveToNext()) {
            Log.d("ChatActivity", "🗨️ " +
                    cursor.getString(0) + " | " +
                    cursor.getString(1) + ": " +
                    cursor.getString(2) + " @ " +
                    cursor.getString(3));
        }
        cursor.close();
        db.close();
    }
    private List<ChatMessage> parseChatResponse(String body) {
        List<ChatMessage> parsed = new ArrayList<>();
        if (body == null || body.trim().isEmpty()) {
            showStatus("❌ Порожня відповідь від сервера", android.R.color.holo_red_dark);
            return parsed;
        }

        try {
            JSONObject json = new JSONObject(body);
            if (json.optInt("status", 0) != 1) {
                showStatus("❌ Невірний статус від сервера", android.R.color.holo_red_dark);
                return parsed;
            }

            JSONArray data = json.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                ChatMessage msg = ChatMessage.fromJsonObject(data.getJSONObject(i));
                parsed.add(msg);
                Log.d("ChatMessage", "✅ Отримано: id=" + msg.getId() + ", автор=" + msg.getAuthor() + ", текст=" + msg.getText());
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "❌ Помилка обробки JSON", e);
            showStatus("❌ JSON помилка", android.R.color.holo_red_dark);
        }

        return parsed;
    }

    private void processChatResponse(List<ChatMessage> parsedMessages) {
        final int[] newCount = {0};  // використовуємо масив для модифікації всередині лямбди

        synchronized (messages) {
            for (ChatMessage msg : parsedMessages) {
                if (messages.stream().noneMatch(existing -> existing.getId().equals(msg.getId()))) {
                    messages.add(msg);
                    insertMessageIntoDb(msg);
                    newCount[0]++;
                }
            }
            messages.sort(Comparator.comparing(ChatMessage::getMoment));
        }

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            Log.i("ChatActivity", "📩 Нових повідомлень з сервера: " + newCount[0]);
        });
    }


    private void showStatus(String message, int colorRes) {
        runOnUiThread(() -> {
            chatStatus.setText(message);
            chatStatus.setTextColor(getResources().getColor(colorRes, getTheme()));
            chatStatus.setVisibility(View.VISIBLE);
            handler.postDelayed(() -> chatStatus.setVisibility(View.GONE), 5000);
        });
    }

    private void playNewMessageSound() {
        if (newMessageSound != null) {
            newMessageSound.start();
            if (bellIcon != null && bellAnimation != null) {
                bellIcon.startAnimation(bellAnimation);
            }
        }
    }

    private void generateTestMessages() {
        String author = etAuthor.getText().toString().trim();
        if (!author.equals(currentUser)) {
            currentUser = author;
            adapter = new ChatMessageAdapter(messages, currentUser);
            configureAdapter();
            recyclerView.setAdapter(adapter);
        }

        Calendar calendar = Calendar.getInstance();
        adapter.addMessage(new ChatMessage("test1", "Андрій", "Сьогоднішнє повідомлення", calendar.getTime()));
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        adapter.addMessage(new ChatMessage("test2", "Ірина", "Вчорашнє повідомлення", calendar.getTime()));
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        adapter.addMessage(new ChatMessage("test3", "Олексій", "2 дні тому", calendar.getTime()));
    }

    private void saveAuthorToFile(String author) {
        try (FileOutputStream fos = openFileOutput(AUTHOR_FILE_NAME, MODE_PRIVATE)) {
            fos.write(author.getBytes(StandardCharsets.UTF_8));
            Log.i("ChatActivity", "💾 Ім’я автора збережено у файл: " + author);
        } catch (IOException e) {
            Log.e("ChatActivity", "❌ Помилка збереження імені", e);
        }
    }

    private String loadAuthorFromFile() {
        try (FileInputStream fis = openFileInput(AUTHOR_FILE_NAME)) {
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        autoUpdateHandler.removeCallbacks(updateRunnable);
        if (newMessageSound != null) newMessageSound.release();
        if (pool != null) pool.shutdownNow();
    }
}