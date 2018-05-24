package com.learninga_z.myfirstapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.models.Conversation;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConversationListAdapter extends ArrayAdapter<Conversation> {

    private Timer timer;

    public ConversationListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ConversationListAdapter(Context context, int resource, List<Conversation> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.conversation_list_row, null);
        }

        Conversation conversation = getItem(position);

        if (conversation != null) {
            TextView name = (TextView) view.findViewById(R.id.convo_name);
            TextView text = (TextView) view.findViewById(R.id.convo_text);
            name.setText(TextUtils.isEmpty(conversation.name) ? conversation.conversationId : conversation.name);
            text.setText(TextUtils.isEmpty(conversation.latestMessage) ? "No messages yet." : conversation.latestMessage);

            View colorCircle = view.findViewById(R.id.convo_color);
            GradientDrawable colorCircleBg = (GradientDrawable) colorCircle.getBackground();

            long milliseconds = conversation.updatedOn.getSeconds() * 1000;
            CharSequence date = DateUtils.formatSameDayTime(milliseconds, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT);

            TextView timeView = view.findViewById(R.id.convo_time);
            timeView.setText(date);

            if(conversation.color != null) {
                colorCircleBg.setColor(Color.parseColor(conversation.color));
            }

        }

        return view;
    }

}