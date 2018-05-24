package com.learninga_z.myfirstapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.learninga_z.myfirstapp.models.Message;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends BaseAdapter {
    private static final String TAG = "MessageAdapter";

    List<Message> messages;
    Map<String,User> users;
    Context context;

    public MessageAdapter(Context context, List<Message> messageList, Map<String,User> userMap) {
        this.context = context;
        messages = messageList;
        users = userMap;
    }

    private class MessageViewHolder {
        public View avatar;
        public TextView name;
        public TextView messageBody;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(position);

        if(message.isMine()) {
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        }
        else {
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (View) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

            User user = users.get(message.getUserId());
            holder.name.setText(user == null ? "" : user.getUsername());
            holder.messageBody.setText(message.getText());

            GradientDrawable avatarCircle = (GradientDrawable) holder.avatar.getBackground();
            avatarCircle.setColor(Color.parseColor("#000000"));
        }

        return convertView;
    }
}