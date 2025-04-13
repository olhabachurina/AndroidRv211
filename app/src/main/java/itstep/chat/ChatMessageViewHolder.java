package itstep.chat;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import itstep.learning.androidrv211.R;
import itstep.learning.orm.ChatMessage;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvAuthor;
    private final TextView tvText;
    private final TextView tvTime;

    public ChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAuthor = itemView.findViewById(R.id.chat_msg_author);
        tvText = itemView.findViewById(R.id.chat_msg_text);
        tvTime = itemView.findViewById(R.id.chat_msg_time);
    }

    public void setChatMessage(ChatMessage chatMessage) {
        tvAuthor.setText(chatMessage.getAuthor());
        tvText.setText(chatMessage.getText());

        String smartTime = getSmartTimeText(chatMessage.getMoment());
        tvTime.setText(smartTime);
    }
    private String getSmartTimeText(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTime(date);

        long diffMillis = now.getTimeInMillis() - then.getTimeInMillis();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (DateUtils.isToday(date.getTime())) {
            return timeFormat.format(date);
        } else if (diffDays == 1) {
            return "Вчора, " + timeFormat.format(date);
        } else if (diffDays <= 6) {
            return getDaysAgoText(diffDays) + ", " + timeFormat.format(date);
        } else {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(date);
        }
    }

    private String getDaysAgoText(long days) {
        if (days == 1) return "1 день тому";
        else if (days >= 2 && days <= 4) return days + " дні тому";
        else return days + " днів тому";
    }
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}