package itstep.learning.androidrv211;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

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

    private static final String chatUrl = "https://chat.momentfor.fun/";
    private final List<ChatMessage> messages = new ArrayList<>();
    private String currentUser = "";

    private TextView chatStatus;
    private RecyclerView recyclerView;
    private EditText etAuthor, etMessage;
    private MediaPlayer newMessageSound;
    private ImageView bellIcon;
    private Animation bellAnimation;

    private ChatMessageAdapter adapter;
    private ExecutorService pool;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler autoUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateChat();
            autoUpdateHandler.postDelayed(this, 30000);         }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        initRecyclerView();
        initSendButton();

        updateChat(); // начальная загрузка
        autoUpdateHandler.postDelayed(updateRunnable, 30000);
    }

    private void initViews() {
        chatStatus = findViewById(R.id.chat_status);
        recyclerView = findViewById(R.id.recycler_messages);
        etAuthor = findViewById(R.id.et_author);
        etMessage = findViewById(R.id.et_message);
        bellIcon = findViewById(R.id.bell_icon);
        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bell);
        newMessageSound = MediaPlayer.create(this, R.raw.bell_sound);

        findViewById(R.id.btn_debug_add).setOnClickListener(v -> generateTestMessages());
        findViewById(R.id.btn_exit).setOnClickListener(v -> finish());
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatMessageAdapter(messages, currentUser);
        recyclerView.setAdapter(adapter);
    }

    private void initSendButton() {
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String author = etAuthor.getText().toString().trim();
            String text = etMessage.getText().toString().trim();

            boolean hasError = false;

            if (author.isEmpty()) {
                etAuthor.setError(getString(R.string.error_empty_author));
                hasError = true;
            } else {
                etAuthor.setError(null);
            }

            if (text.isEmpty()) {
                etMessage.setError(getString(R.string.error_empty_message));
                hasError = true;
            } else {
                etMessage.setError(null);
            }

            if (hasError) return;

            if (!author.equals(currentUser)) {
                currentUser = author;
                adapter = new ChatMessageAdapter(messages, currentUser);
                recyclerView.setAdapter(adapter);

                // ✅ Блокуємо поле автора після першого повідомлення
                etAuthor.setEnabled(false);
                etAuthor.setFocusable(false);
                etAuthor.setFocusableInTouchMode(false);
            }

            ChatMessage message = new ChatMessage("0", author, text, new Date());
            adapter.addMessage(message);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            playNewMessageSound();

            // Відправка повідомлення на сервер
            CompletableFuture.runAsync(() -> {
                String response = Services.sendMessageToServer(chatUrl, author, text);

                if (response == null) {
                    runOnUiThread(() ->
                            showStatus(getString(R.string.error_network), android.R.color.holo_red_dark));
                    return;
                }

                Log.d("ChatActivity", "Server response: " + response);

                if (response.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        showStatus(getString(R.string.status_message_sent), android.R.color.holo_green_dark);
                        etMessage.setText(""); // ✅ очищення тільки після успіху
                        updateChat();
                    });
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response);
                    int status = json.optInt("status", 0);

                    if (status == 1) {
                        runOnUiThread(() -> {
                            showStatus(getString(R.string.status_message_sent), android.R.color.holo_green_dark);
                            etMessage.setText(""); // ✅ очищення тільки після успіху
                            updateChat();
                        });
                    } else {
                        runOnUiThread(() ->
                                showStatus(getString(R.string.error_server_status, status), android.R.color.holo_red_dark));
                    }
                } catch (Exception e) {
                    Log.e("ChatActivity", "JSON parse error: " + e.getMessage());
                    runOnUiThread(() ->
                            showStatus(getString(R.string.error_json), android.R.color.holo_red_dark));
                }
            });
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
            recyclerView.setAdapter(adapter);
        }

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        adapter.addMessage(new ChatMessage("test1", "Андрій", "Сьогоднішнє повідомлення", today));

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();
        adapter.addMessage(new ChatMessage("test2", "Ірина", "Вчорашнє повідомлення", yesterday));

        calendar.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysAgo = calendar.getTime();
        adapter.addMessage(new ChatMessage("test3", "Олексій", "2 дні тому", twoDaysAgo));

        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date aWeekAgo = calendar.getTime();
        adapter.addMessage(new ChatMessage("test4", "Ольга", "Повідомлення тиждень тому", aWeekAgo));

        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void updateChat() {
        Log.i("ChatActivity", "updateChat update");

        handler.postDelayed(() -> {
            pool = Executors.newFixedThreadPool(3);
            CompletableFuture
                    .supplyAsync(() -> Services.fetchUrl(chatUrl), pool)
                    .thenApply(this::parseChatResponse)
                    .thenAccept(this::processChatResponse);
        }, 2000); // 2 секунды задержки перед загрузкой
    }

    private List<ChatMessage> parseChatResponse(String body) {
        List<ChatMessage> parsed = new ArrayList<>();

        if (body == null || body.trim().isEmpty()) {
            showStatus(getString(R.string.error_empty_response), android.R.color.holo_red_dark);
            return parsed;
        }

        try {
            JSONObject json = new JSONObject(body);
            int status = json.optInt("status", 0);

            if (status != 1) {
                showStatus(getString(R.string.error_server_status, status), android.R.color.holo_red_dark);
                return parsed;
            }

            showStatus(getString(R.string.status_success_loaded), android.R.color.holo_green_dark);
            JSONArray data = json.getJSONArray("data");

            for (int i = 0; i < data.length(); i++) {
                parsed.add(ChatMessage.fromJsonObject(data.getJSONObject(i)));
            }

            Log.d("ChatActivity", "Parsed " + parsed.size() + " messages from server");

        } catch (Exception e) {
            Log.e("ChatActivity", "JSON Exception: " + e.getMessage());
            showStatus(getString(R.string.error_json), android.R.color.holo_red_dark);
        }

        return parsed;
    }

    private void processChatResponse(List<ChatMessage> parsedMessages) {
        int oldSize = messages.size();
        for (ChatMessage msg : parsedMessages) {
            boolean isNew = messages.stream()
                    .noneMatch(existing -> existing.getId().equals(msg.getId()));
            if (isNew) messages.add(msg);
        }
        int newSize = messages.size();

        messages.sort(Comparator.comparing(ChatMessage::getMoment));

        runOnUiThread(() -> {
            adapter.notifyItemRangeChanged(oldSize, newSize);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });
    }

    private void showStatus(String message, int colorRes) {
        runOnUiThread(() -> {
            chatStatus.setText(message);
            chatStatus.setTextColor(getResources().getColor(colorRes));
            chatStatus.setVisibility(View.VISIBLE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            hideStatusAfterDelay();
        });
    }

    private void hideStatusAfterDelay() {
        handler.postDelayed(() -> {
            if (chatStatus != null) chatStatus.setVisibility(View.GONE);
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Остановить все задержанные действия в основном handler-е
        handler.removeCallbacksAndMessages(null);

        // Освободить звук
        if (newMessageSound != null) {
            newMessageSound.release();
        }

        // Остановить потокобработчик
        if (pool != null) {
            pool.shutdownNow();
        }

        // Остановить автообновление чата
        autoUpdateHandler.removeCallbacks(updateRunnable);
    }
}