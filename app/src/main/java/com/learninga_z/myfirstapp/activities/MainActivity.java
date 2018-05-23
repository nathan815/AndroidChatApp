package com.learninga_z.myfirstapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.activities.ConversationListActivity;
import com.learninga_z.myfirstapp.activities.LoginActivity;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";


    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Log.d(TAG, "Main activity started");

        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean logout = getIntent().getBooleanExtra("logout", false);
        if(logout) {
            Log.v(TAG, "logout:start");
            auth.signOut();
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        Intent i;
        if(currentUser == null) {
            i = new Intent(this, LoginActivity.class);
        }
        else {
            i = new Intent(this, ConversationListActivity.class);
        }

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Log.d(TAG, "Starting activity: " + i.toString());

        startActivity(i);
        finish();
    }

}
