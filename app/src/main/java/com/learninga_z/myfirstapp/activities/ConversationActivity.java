package com.learninga_z.myfirstapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.adapters.MessageAdapter;
import com.learninga_z.myfirstapp.models.Conversation;
import com.learninga_z.myfirstapp.models.Message;
import com.learninga_z.myfirstapp.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {

    private final static String TAG = "ConversationActivity";

    // UI
    private ListView messagesView;
    private EditText sendMessageText;

    private String conversationId;

    private CollectionReference usersRef;
    private DocumentReference conversationRef;
    private CollectionReference messagesRef;
    private ListenerRegistration conversationListener;
    private ListenerRegistration messageListener;

    private MessageAdapter messageAdapter;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private List<Message> messageList = new ArrayList<>();
    private Map<String, User> userMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        conversationId = intent != null ? intent.getStringExtra("conversation_id") : "";

        String conversationName = intent != null ? intent.getStringExtra("conversation_name") : "Conversation";
        setTitle(conversationName);

        sendMessageText = findViewById(R.id.send_message_text);
        messagesView = findViewById(R.id.messages_view);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        usersRef = db.collection("users");
        conversationRef = db.collection("conversations").document(conversationId);
        messagesRef = conversationRef.collection("messages");

        ImageButton sendMessageButton = findViewById(R.id.send_button);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        messagesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        listenForConversationInfo();
        listenForMessages();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        conversationListener.remove();
        messageListener.remove();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void hideKeyboard() {
        sendMessageText.clearFocus();
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sendMessageText.getWindowToken(), 0);
    }

    private void listenForConversationInfo() {
        conversationListener = conversationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                userMap.clear();
                if(snapshot != null) {
                    Log.d(TAG, "conversation updated, snapshot: " + snapshot);
                    Conversation conversation = snapshot.toObject(Conversation.class);
                    for (Map.Entry<String, Boolean> item : conversation.getUsers().entrySet()) {
                        fetchUserInfo(item.getKey());
                    }
                }
            }
        });
    }

    private void fetchUserInfo(final String userId) {
        Log.d(TAG, "fetchUserInfo: fetching "+userId);
        usersRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "User data: " + document.getData());
                        User user = document.toObject(User.class);
                        userMap.put(userId, user);

                        // notify the message adapter
                        if(messageAdapter != null)
                            messageAdapter.notifyDataSetChanged();
                    }
                    else {
                        Log.d(TAG, "No such user " + userId);
                    }
                }
                else {
                    Log.d(TAG, "get user info failed with ", task.getException());
                    fetchUserInfo(userId); // retry
                }
            }
        });
    }

    private void showNoMessages() {
        findViewById(R.id.conversation_no_messages).setVisibility(View.VISIBLE);
    }
    private void hideNoMessages() {
        findViewById(R.id.conversation_no_messages).setVisibility(View.GONE);
    }

    private void listenForMessages() {
        messageList.clear();
        messageAdapter = new MessageAdapter(getApplicationContext(), messageList, userMap);
        messagesView.setAdapter(messageAdapter);
        messageListener = messagesRef
        .orderBy("sentOn", Query.Direction.ASCENDING)
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots.size() == 0) {
                    showNoMessages();
                }
                else {
                    hideNoMessages();
                }
                if(documentSnapshots != null) {
                    for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                        Message message = dc.getDocument().toObject(Message.class);
                        message.setIsMine(message.getUserId().equals(currentUser.getUid()));
                        switch (dc.getType()) {
                            case ADDED:
                                messageList.add(message);
                                Log.v(TAG, "Added message: " + message);
                                break;
                            case MODIFIED:
                                // messages don't get modified
                                break;
                            case REMOVED:
                                // messages don't get removed
                                break;
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesView.setSelection(messageAdapter.getCount() - 1);
            }
        });
    }

    public void sendMessage() {
        final String messageText = sendMessageText.getText().toString();
        if(messageText.length() > 0) {
            Message message = new Message(currentUser.getUid(), messageText);
            DocumentReference doc = messagesRef.document();
            doc.set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Message sent: " + messageText);
                        sendMessageText.getText().clear();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error sending message", e);
                    }
                });
        }
    }

}
