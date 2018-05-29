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
import com.google.firebase.Timestamp;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {

    private final static String TAG = "ConversationActivity";

    // UI
    private ListView messagesListView;
    private EditText sendMessageTextField;

    private String conversationId;
    private Conversation conversation;

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
        conversation = intent != null ? (Conversation) intent.getParcelableExtra("conversation") : null;
        if(conversation != null) {
            conversationId = conversation.conversationId;
        }

        setTitle(conversation.name);

        sendMessageTextField = findViewById(R.id.send_message_text);
        messagesListView = findViewById(R.id.messages_view);

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

        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
            }
        });

        sendMessageTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    return;
                }
                // delayed scroll to bottom to allow keyboard to open first
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scrollToBottom();
                                }
                            });
                        } catch (InterruptedException e) {
                        }
                    }
                }.start();
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

    private void fetchUserInfo(final String userId, final int retries) {
        Log.d(TAG, "fetchUserInfo: fetching "+userId);
        final int maxRetries = 5;
        if(retries > maxRetries) {
            Log.w(TAG, "fetchUserInfo: max retries exceeded for user " + userId);
            return;
        }
        usersRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        userMap.put(userId, user);

                        // notify the message adapter
                        if(messageAdapter != null)
                            messageAdapter.notifyDataSetChanged();
                    }
                    else {
                        Log.w(TAG, "fetchUserInfo: No such user " + userId);
                    }
                }
                else {
                    Log.w(TAG, "fetchUserInfo: get user info failed with ", task.getException());
                    fetchUserInfo(userId, retries + 1);
                }
            }
        });
    }
    private void fetchUserInfo(String userId) {
        fetchUserInfo(userId, 0);
    }

    private void scrollToBottom() {
        messagesListView.setSelection(messageAdapter.getCount() - 1);
    }

    private void hideKeyboard() {
        sendMessageTextField.clearFocus();
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sendMessageTextField.getWindowToken(), 0);
    }

    private void showNoMessages() {
        findViewById(R.id.conversation_no_messages).setVisibility(View.VISIBLE);
    }
    private void hideNoMessages() {
        findViewById(R.id.conversation_no_messages).setVisibility(View.GONE);
    }

    private void listenForConversationInfo() {
        conversationListener = conversationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                userMap.clear();
                if(snapshot != null) {
                    Log.d(TAG, "conversation info fetched, snapshot: " + snapshot);
                    Conversation conversation = snapshot.toObject(Conversation.class);
                    for (String userId : conversation.getUsers().keySet()) {
                        fetchUserInfo(userId);
                    }
                }
            }
        });
    }

    private void listenForMessages() {
        messageList.clear();
        messageAdapter = new MessageAdapter(getApplicationContext(), messageList, userMap);
        messagesListView.setAdapter(messageAdapter);
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
                    processMessageSnapshots(documentSnapshots);
                }
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        });
    }

    private void processMessageSnapshots(QuerySnapshot snapshots) {
        if(snapshots == null) {
            return;
        }
        Timestamp lastTimestamp = null;
        if(!messageList.isEmpty()) {
            Message lastMessage = messageList.get(messageList.size()-1);
            lastTimestamp = lastMessage.getSentOn();
        }
        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            Message message = dc.getDocument().toObject(Message.class);
            message.setIsMine(message.getUserId().equals(currentUser.getUid()));

            // if this message is a different day from previous message, add a message "date header"
            if(lastTimestamp == null || !isSameDay(lastTimestamp.toDate(), message.getSentOn().toDate()) ) {
                messageList.add(new Message(Message.TYPE_DATE_HEADER, null, null, message.getSentOn()));
                lastTimestamp = message.getSentOn();
            }

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

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    private void sendMessage() {
        final String messageText = sendMessageTextField.getText().toString();
        if(messageText.length() > 0) {
            Message message = new Message(currentUser.getUid(), messageText);
            DocumentReference doc = messagesRef.document();
            doc.set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Message sent: " + messageText);
                        sendMessageTextField.getText().clear();
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
