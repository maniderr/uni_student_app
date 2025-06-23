package com.lock.student_chat;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StudentChatMessageAdapter extends RecyclerView.Adapter<StudentChatMessageAdapter.MessageViewHolder> {
    private final List<ChatMessage> messages;
    private final String currentUser;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


    public StudentChatMessageAdapter(List<ChatMessage> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userText, messageText, timeText;
        LinearLayout messageContainer;

        public MessageViewHolder(View itemView) {
            super(itemView);
            userText = itemView.findViewById(R.id.tvUser);
            messageText = itemView.findViewById(R.id.tvMessage);
            timeText = itemView.findViewById(R.id.tvTime);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        holder.messageText.setText(message.getMessage());
        holder.userText.setText(message.getUser());

        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date utcDate = utcFormat.parse(message.getMsg_time());

            SimpleDateFormat localFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            localFormat.setTimeZone(TimeZone.getDefault()); // Device's timezone

            String localTime = localFormat.format(utcDate);
            holder.timeText.setText(localTime);
        } catch (Exception e) {
            holder.timeText.setText(message.getMsg_time());
            Log.e("ChatAdapter", "Error parsing timestamp", e);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();

        if (message.getUser().equals(currentUser)) {
            // Current user's message - align right
            params.gravity = Gravity.END;
            holder.messageContainer.setBackgroundResource(R.drawable.message_bubble_sent);
        } else {
            // Other user's message - align left
            params.gravity = Gravity.START;
            holder.messageContainer.setBackgroundResource(R.drawable.message_bubble_received);
        }
        holder.messageContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }
}