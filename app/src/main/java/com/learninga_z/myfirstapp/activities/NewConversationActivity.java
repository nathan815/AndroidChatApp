package com.learninga_z.myfirstapp.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.models.Conversation;

public class NewConversationActivity extends AppCompatActivity {

    private static final String TAG = "NewConversationActivity";

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    EditText nameView, otherUserView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        nameView = findViewById(R.id.new_convo_name);
        otherUserView = findViewById(R.id.other_user); // temp

        Button createbutton = findViewById(R.id.new_convo_create);
        createbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreation();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = auth.getCurrentUser();
    }

    private void attemptCreation() {
        String name = nameView.getText().toString();
        String myId = currentUser.getUid();
        String otherId = otherUserView.getText().toString();
        create(name, myId, otherId);
    }

    private void create(String name, String myId, String otherId) {
        CollectionReference conversationsRef = db.collection("conversations");
        DocumentReference document = conversationsRef.document();
        Conversation conversation = new Conversation(document.getId(), name);
        conversation.users.put(myId, true);
        conversation.users.put(otherId, true);

        document.set(conversation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Created convo");
                        finish();
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
