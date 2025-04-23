package itstep.learning.androidrv211;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
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

import androidx.annotation.NonNull;
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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private static final String DB_NAME = "chat_db";
    private static final String CHANNEL_ID = "CHAT-CHANNEL";

    private static final String TABLE_AUTHORS = "authors";
    private static final String TABLE_HISTORY = "chat_history";

    private final List<ChatMessage> messages = new ArrayList<>();
    private String currentUser = "";

    private TextView chatStatus;
    private RecyclerView recyclerView;
    private EditText etAuthor, etMessage;
    private CheckBox cbRememberMe;
    private MediaPlayer newMessageSound;
    private ImageView bellIcon;
    private Animation bellAnimation;
    private Button btnSend, btnClearAuthor, btnShowNotification;
    private ChatMessageAdapter adapter;
    private ExecutorService pool;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler autoUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        registerChannel();
        requestNotificationPermission();

        initDatabase();
        loadMessagesFromDbAsync();
        initViews();
        initRecyclerView();
        initSendButton();
        initShowNotificationButton();

        updateRunnable = () -> {
            updateChat();
            autoUpdateHandler.postDelayed(updateRunnable, 30000);
        };

        updateChat();
        autoUpdateHandler.postDelayed(updateRunnable, 30000);
    }

    private void registerChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications about new incoming messages");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        234
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 234) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, " Дозвіл на повідомлення надано", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, " Ви не будете бачити повідомлення", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makeNotification(String title, String content) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_chat_24)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void updateChat() {
        handler.postDelayed(() -> {
            pool = Executors.newFixedThreadPool(3);
            CompletableFuture
                    .supplyAsync(() -> Services.fetchUrl(CHAT_URL), pool)
                    .thenApply(this::parseChatResponse)
                    .thenAccept(this::processChatResponse);
        }, 2000);
    }

    private List<ChatMessage> parseChatResponse(String body) {
        List<ChatMessage> parsed = new ArrayList<>();
        if (body == null || body.trim().isEmpty()) return parsed;

        try {
            JSONObject json = new JSONObject(body);
            if (json.optInt("status", 0) != 1) return parsed;

            JSONArray data = json.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                ChatMessage msg = ChatMessage.fromJsonObject(data.getJSONObject(i));
                parsed.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsed;
    }

    private void processChatResponse(List<ChatMessage> parsedMessages) {
        final int[] newCount = {0};

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
            if (newCount[0] > 0) {
                makeNotification("Нове повідомлення 📩", "У вас є " + newCount[0] + " нових повідомлень");
            }
        });
    }

    private void initDatabase() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AUTHORS + " (name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (id TEXT, author TEXT, text TEXT, moment TEXT)");
        db.close();
    }

    private void insertMessageIntoDb(ChatMessage msg) {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        ContentValues values = new ContentValues();
        values.put("id", msg.getId());
        values.put("author", msg.getAuthor());
        values.put("text", msg.getText());
        values.put("moment", ChatMessage.dateFormat.format(msg.getMoment()));
        db.insert(TABLE_HISTORY, null, values);
        db.close();
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
        btnShowNotification = findViewById(R.id.btn_show_notification);

        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bell);
        newMessageSound = MediaPlayer.create(this, R.raw.bell_sound);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(messages, currentUser);
        recyclerView.setAdapter(adapter);
    }

    private void initSendButton() {
        btnSend.setOnClickListener(v -> {
            String author = etAuthor.getText().toString().trim();
            String text = etMessage.getText().toString().trim();
            if (author.isEmpty() || text.isEmpty()) return;

            ChatMessage message = new ChatMessage("0", author, text, new Date());
            adapter.addMessage(message);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            playNewMessageSound();

            CompletableFuture.runAsync(() -> {
                String response = Services.sendMessageToServer(CHAT_URL, author, text);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.optInt("status", 0) == 1) {
                        runOnUiThread(() -> etMessage.setText(""));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void initShowNotificationButton() {
        btnShowNotification.setOnClickListener(v -> {
            makeNotification(" Привет!", "Это уведомление чата.");
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

    private void loadMessagesFromDbAsync() {
        CompletableFuture.runAsync(() -> {
            SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("SELECT id, author, text, moment FROM " + TABLE_HISTORY, null);
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
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                messages.sort(Comparator.comparing(ChatMessage::getMoment));
            }
            cursor.close();
            db.close();

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            });
        });
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
