package com.learninga_z.myfirstapp;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.models.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends AppCompatActivity {

    private static final String TAG = "ConversationList";

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;

    ListView listView;
    private List<Conversation> convoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        convoList = new ArrayList<>();
        listView = findViewById(R.id.convo_list);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        listenForConversations();
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
        db.collection("conversations").orderBy("createdOn")
          .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    convoList.clear();
                    for (DocumentSnapshot snapshot : documentSnapshots){
                        Conversation convo = snapshot.toObject(Conversation.class);
                        convoList.add(convo);
                    }
                    CustomAdapter adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_selectable_list_item, convoList);
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
        Conversation convo = new Conversation(user.getUid(), "other");
        DocumentReference ref = db.collection("conversations").document();
        convo.conversationId = ref.getId();

        ref.set(convo)
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

class CustomAdapter extends ArrayAdapter<Conversation> {

    public CustomAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapter(Context context, int resource, List<Conversation> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.conversation_list_row, null);
        }

        Conversation c = getItem(position);

        if (c != null) {
            TextView name = (TextView) v.findViewById(R.id.convo_name);
            TextView text = (TextView) v.findViewById(R.id.convo_text);

            name.setText(TextUtils.isEmpty(c.name) ? c.conversationId : c.name);
            text.setText(TextUtils.isEmpty(c.latestMessage) ? "No messages yet." : c.latestMessage);
        }

        return v;
    }

}