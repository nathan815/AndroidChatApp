package com.learninga_z.myfirstapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.models.Message;
import com.learninga_z.myfirstapp.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        public TextView timestamp;
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

        Date date = message.getSentOn().toDate();

        if(message.getMessageType() == Message.TYPE_DATE_HEADER) {
            convertView = messageInflater.inflate(R.layout.message_date_header, parent, false);
            holder.timestamp = convertView.findViewById(R.id.message_date_header_timestamp);
            convertView.setTag(holder);
            holder.timestamp.setText(getDayString(date));
        }
        else if(message.isMine()) {
            convertView = messageInflater.inflate(R.layout.my_message, parent, false);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            holder.timestamp = convertView.findViewById(R.id.message_timestamp);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
            holder.timestamp.setText(getTimeString(date));
        }
        else {
            convertView = messageInflater.inflate(R.layout.their_message, parent, false);
            holder.avatar = (View) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            holder.timestamp = convertView.findViewById(R.id.message_timestamp);
            convertView.setTag(holder);

            User user = users.get(message.getUserId());
            holder.name.setText(user == null ? "" : user.getUsername());
            holder.messageBody.setText(message.getText());
            holder.timestamp.setText(getTimeString(date));

            GradientDrawable avatarCircle = (GradientDrawable) holder.avatar.getBackground();
            avatarCircle.setColor(Color.parseColor("#000000"));
        }

        return convertView;
    }

    private String getDayString(Date date) {
        if(date == null) {
            return "";
        }
        if(DateUtils.isToday(date.getTime())) {
            return "Today";
        }
        SimpleDateFormat localDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        return localDateFormat.format(date);
    }

    private String getTimeString(Date date) {
        if(date == null)
            return "";
        SimpleDateFormat localDateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        return localDateFormat.format(date);
    }
}