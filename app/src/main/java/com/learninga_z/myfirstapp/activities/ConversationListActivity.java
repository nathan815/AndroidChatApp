package com.learninga_z.myfirstapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.adapters.ConversationListAdapter;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.models.Conversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationListActivity extends AppCompatActivity {

    private static final String TAG = "ConversationList";

    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    View progressOverlayView;
    ListView listView;
    private List<Conversation> convoList = new ArrayList<>();

    private CollectionReference conversationsRef;
    private ListenerRegistration conversationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        progressOverlayView = findViewById(R.id.progress_overlay);
        listView = findViewById(R.id.convo_list);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        conversationsRef = db.collection("conversations");

        registerListViewClickHandler();
    }

    private void registerListViewClickHandler() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Conversation conversation = convoList.get(position);
                openConversation(conversation);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        conversationListener.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
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

    private void openConversation(Conversation conversation) {
        Intent intent = new Intent(ConversationListActivity.this, ConversationActivity.class);
        intent.putExtra("conversation_id", conversation.conversationId);
        intent.putExtra("conversation_name", conversation.name);
        startActivity(intent);
    }

    private void listenForConversations() {
        conversationListener = conversationsRef
        .whereEqualTo("users." + currentUser.getUid(),true)
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                convoList.clear();
                if(documentSnapshots != null) {
                    for (DocumentSnapshot snapshot : documentSnapshots) {
                        Conversation conversation = snapshot.toObject(Conversation.class);
                        convoList.add(conversation);
                    }
                }
                sortConversations();
                ConversationListAdapter adapter = new ConversationListAdapter(getApplicationContext(), android.R.layout.simple_selectable_list_item, convoList);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }
        });
    }

    private void sortConversations() {
        Collections.sort(convoList, new Comparator<Conversation>() {
            public int compare(Conversation c1, Conversation c2) {
                if(c1.updatedOn == null || c2.updatedOn == null)
                    return 0;
                return c1.updatedOn.compareTo(c2.updatedOn);
            }
        });
        Collections.reverse(convoList);
    }

    private void logout() {
        Intent i = new Intent(ConversationListActivity.this, LauncherActivity.class);
        i.putExtra("logout", true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void newConversation() {
        Intent i = new Intent(ConversationListActivity.this, NewConversationActivity.class);
        startActivity(i);
    }

}