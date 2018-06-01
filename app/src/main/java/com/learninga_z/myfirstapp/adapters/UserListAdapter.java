package com.learninga_z.myfirstapp.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.models.User;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    public UserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public UserListAdapter(Context context, int resource, List<User> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.user_list_row, null);
        }

        User user = getItem(position);

        if (user != null) {
            TextView username = (TextView) view.findViewById(R.id.username);
            TextView email = (TextView) view.findViewById(R.id.email);
            username.setText(TextUtils.isEmpty(user.username) ? "No Username" : user.username);
            email.setText(TextUtils.isEmpty(user.email) ? "" : user.email);
        }

        return view;
    }

}
