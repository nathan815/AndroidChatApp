package com.learninga_z.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.models.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends AppCompatActivity {

    private static final String TAG = "ConversationList";

    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    ListView listView;
    private List<Conversation> convoList;

    private CollectionReference conversationsRef;
    private ListenerRegistration conversationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        convoList = new ArrayList<>();
        listView = findViewById(R.id.convo_list);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        conversationsRef = db.collection("conversations");

        listenForConversations();
    }

    @Override
    protected void onStop() {
        super.onStop();
        conversationListener.remove();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversation_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_action_logout:
                logout();
                return true;
            case R.id.menu_action_new_convo:
                newConversation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void listenForConversations() {
        conversationListener = conversationsRef.orderBy("createdOn").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            convoList.clear();
            for (DocumentSnapshot snapshot : documentSnapshots){
                Conversation convo = snapshot.toObject(Conversation.class);
                convoList.add(convo);
            }
            ConversationListAdapter adapter = new ConversationListAdapter(getApplicationContext(), android.R.layout.simple_selectable_list_item, convoList);
            adapter.notifyDataSetChanged();
            listView.setAdapter(adapter);
            }
        });
    }

    private void logout() {
        Intent i = new Intent(ConversationListActivity.this, MainActivity.class);
        i.putExtra("logout", true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void newConversation() {
        DocumentReference document = conversationsRef.document();
        Conversation conversation = new Conversation(document.getId());
        conversation.users.put(currentUser.getUid(), true);

        document.set(conversation)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                 @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Created convo");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding convo", e);
                }
            });
    }

}

class ConversationListAdapter extends ArrayAdapter<Conversation> {

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
            View colorCircle = view.findViewById(R.id.convo_color);
            GradientDrawable colorCircleBg = (GradientDrawable) colorCircle.getBackground();

            if(conversation.color != null) {
                colorCircleBg.setColor(Color.parseColor(conversation.color));
            }

            name.setText(TextUtils.isEmpty(conversation.name) ? conversation.conversationId : conversation.name);
            text.setText(TextUtils.isEmpty(conversation.latestMessage) ? "No messages yet." : conversation.latestMessage);
        }

        return view;
    }

}