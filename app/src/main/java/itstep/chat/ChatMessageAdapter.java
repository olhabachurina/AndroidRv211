package itstep.chat;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;

import itstep.learning.androidrv211.R;
import itstep.learning.orm.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    private final List<ChatMessage> messages;
    private final String currentUser;

    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

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

        // Выравнивание и фон
        LinearLayout layout = holder.itemView.findViewById(R.id.chat_msg_layout);
        LinearLayout bubble = holder.itemView.findViewById(R.id.chat_bubble);
        if (message.getAuthor().equalsIgnoreCase(currentUser)) {
            layout.setGravity(Gravity.END);
            bubble.setBackgroundResource(R.drawable.item_background_user);
        } else {
            layout.setGravity(Gravity.START);
            bubble.setBackgroundResource(R.drawable.item_background);
        }

        // Обработка кликов
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(message);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(message);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        messages.sort(Comparator.comparing(ChatMessage::getMoment));
        notifyItemInserted(messages.indexOf(message));
    }

    // Слушатели событий
    public interface OnItemClickListener {
        void onClick(ChatMessage message);
    }

    public interface OnItemLongClickListener {
        void onLongClick(ChatMessage message);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}
