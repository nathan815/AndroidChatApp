package com.learninga_z.myfirstapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.adapters.MessageAdapter;
import com.learninga_z.myfirstapp.R;
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
                sendMessageText.clearFocus();
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sendMessageText.getWindowToken(), 0);
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
    protected void onStop() {
        super.onStop();
        conversationListener.remove();
        messageListener.remove();
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

    private void listenForMessages() {
        messageListener = messagesRef
                .orderBy("sentOn", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        messageList.clear();
                        if(documentSnapshots != null) {
                            for (DocumentSnapshot snapshot : documentSnapshots) {
                                Message message = snapshot.toObject(Message.class);
                                message.setIsMine(message.getUserId().equals(currentUser.getUid()));
                                messageList.add(message);
                            }
                        }
                        messageAdapter = new MessageAdapter(getApplicationContext(), messageList, userMap);
                        messageAdapter.notifyDataSetChanged();
                        messagesView.setAdapter(messageAdapter);
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

//    @Override
//    public void onMessage(Room room, final JsonNode json, final Member member) {
//        Log.d(TAG, json.toString());
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            MemberData memberData = mapper.treeToValue(member.getClientData(), MemberData.class);
//            boolean belongsToCurrentUser = member.getId().equals(scaledrone.getClientID());
//            final Message message = new Message(json.asText(), memberData, belongsToCurrentUser);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    messageAdapter.add(message);
//                    messagesView.setSelection(messagesView.getCount() - 1);
//                }
//            });
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }

}
