package itstep.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;

import itstep.learning.androidrv211.R;
import itstep.learning.orm.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {

    private final List<ChatMessage> messages;
    private final String currentUser;

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_msg_layout, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.setChatMessage(message);


        LinearLayout layout = holder.itemView.findViewById(R.id.chat_msg_layout);
        LinearLayout bubble = holder.itemView.findViewById(R.id.chat_bubble);

        if (message.getAuthor().equalsIgnoreCase(currentUser)) {
            layout.setGravity(Gravity.END);
            bubble.setBackgroundResource(R.drawable.item_background_user); // фон для себе
        } else {
            layout.setGravity(Gravity.START);
            bubble.setBackgroundResource(R.drawable.item_background); // фон для інших
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        messages.sort(Comparator.comparing(ChatMessage::getMoment));
        notifyDataSetChanged();
    }
}
