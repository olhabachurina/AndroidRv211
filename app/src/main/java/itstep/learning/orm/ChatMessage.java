package itstep.learning.orm;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessage {
    public static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private String id;
    private String author;
    private String text;
    private Date moment;

    public ChatMessage() {}

    public ChatMessage(String id, String author, String text, Date moment) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.moment = moment;
    }

    public static ChatMessage fromJsonObject(JSONObject jsonObject) {
        ChatMessage chatMessage = new ChatMessage();
        try {
            chatMessage.id = jsonObject.getString("id");
            chatMessage.author = jsonObject.getString("author");
            chatMessage.text = jsonObject.getString("text");

            String momentStr = jsonObject.getString("moment");
            chatMessage.moment = dateFormat.parse(momentStr);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return chatMessage;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Date getMoment() {
        return moment;
    }

    @Override
    public String toString() {
        return "[" + dateFormat.format(moment) + "] " + author + ": " + text;
    }
    public static List<ChatMessage> fromJsonArray(JSONArray jsonArray) {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.optJSONObject(i);
            if (obj != null) {
                messages.add(ChatMessage.fromJsonObject(obj));
            }
        }
        return messages;
    }
}

/*{
        "id": "3496",
        "author": "3",
        "text": "vbabv",
        "moment": "2025-04-03 21:20:26"
        },
        */
