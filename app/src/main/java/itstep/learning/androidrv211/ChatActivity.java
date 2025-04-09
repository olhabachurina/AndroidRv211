package itstep.learning.androidrv211;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final String chatUrl = "https://chat.momentfor.fun/";
    private ExecutorService pool;

    private TextView chatStatus;
    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private EditText etAuthor;
    private EditText etMessage;
    private MediaPlayer newMessageSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatStatus = findViewById(R.id.chat_status);
        messagesContainer = findViewById(R.id.messages_container);
        scrollView = findViewById(R.id.scrollView);
        etAuthor = findViewById(R.id.et_author);
        etMessage = findViewById(R.id.et_message);
        ImageButton btnSend = findViewById(R.id.btn_send);
        ImageButton btnExit = findViewById(R.id.btn_exit);

        newMessageSound = MediaPlayer.create(this, R.raw.bell_sound);

        btnExit.setOnClickListener(v -> finish());

        pool = Executors.newFixedThreadPool(3);
        CompletableFuture
                .supplyAsync(() -> Services.fetchUrl(chatUrl), pool)
                .thenApply(this::parseChatResponse)
                .thenAccept(this::processChatResponse);

        btnSend.setOnClickListener(v -> {
            String author = etAuthor.getText().toString().trim();
            String text = etMessage.getText().toString().trim();
            if (!author.isEmpty() && !text.isEmpty()) {
                ChatMessage msg = new ChatMessage("0", author, text, new Date());
                addMessageToLayout(msg, true);
                etMessage.setText("");
            }
        });
    }

    private List<ChatMessage> parseChatResponse(String body) {
        List<ChatMessage> messages = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(body);

            int status = jsonObject.optInt("status", 0);
            if (status != 1) {
                String warning = "❌ Сервер повернув статус: " + status;
                Log.w("ChatActivity", warning);

                runOnUiThread(() -> {
                    chatStatus.setText(warning);
                    chatStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    chatStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(ChatActivity.this, warning, Toast.LENGTH_LONG).show();
                    hideStatusAfterDelay();
                });

                return messages;
            }

            runOnUiThread(() -> {
                chatStatus.setText("✅ Повідомлення успішно отримано");
                chatStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                chatStatus.setVisibility(View.VISIBLE);
                Toast.makeText(ChatActivity.this, "✅ Повідомлення отримано", Toast.LENGTH_SHORT).show();
                hideStatusAfterDelay();
            });

            JSONArray arr = jsonObject.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ChatMessage msg = ChatMessage.fromJsonObject(obj);
                messages.add(msg);
            }

        } catch (JSONException e) {
            Log.e("ChatActivity", "JSON Exception: " + e.getMessage());

            runOnUiThread(() -> {
                chatStatus.setText("❌ Помилка обробки JSON");
                chatStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                chatStatus.setVisibility(View.VISIBLE);
                Toast.makeText(ChatActivity.this, "❌ Помилка обробки JSON", Toast.LENGTH_LONG).show();
                hideStatusAfterDelay();
            });
        }

        return messages;
    }

    private void hideStatusAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (chatStatus != null) {
                chatStatus.setVisibility(View.GONE);
            }
        }, 5000); // 5 секунд
    }

    private void processChatResponse(List<ChatMessage> messages) {
        runOnUiThread(() -> {
            for (ChatMessage msg : messages) {
                addMessageToLayout(msg, false);
            }
        });
    }

    private void addMessageToLayout(ChatMessage msg, boolean playSound) {
        TextView tv = new TextView(this);
        tv.setText(msg.toString());
        tv.setTextSize(16);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tv.setPadding(24, 16, 24, 16);
        tv.setBackgroundResource(R.drawable.chat_message_background);


        String currentAuthor = etAuthor.getText().toString().trim();
        if (msg.getAuthor().equalsIgnoreCase(currentAuthor)) {
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.my_message_bg));  // напр. светло-зеленый
        } else {
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.other_message_bg));  // напр. светло-серый
        }


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(24, 8, 24, 8);
        tv.setLayoutParams(params);

        messagesContainer.addView(tv);


        scrollView.postDelayed(() ->
                scrollView.fullScroll(View.FOCUS_DOWN), 100);

        // Звук
        if (playSound && newMessageSound != null) {
            newMessageSound.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newMessageSound != null) {
            newMessageSound.release();
        }
        if (pool != null) {
            pool.shutdownNow();
        }
    }
}