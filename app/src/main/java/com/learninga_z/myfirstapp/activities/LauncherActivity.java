package com.learninga_z.myfirstapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends Activity {

    private static final String TAG = "LauncherActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {

        Log.d(TAG, "Launcher activity started");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        super.onStart();

        boolean logout = getIntent().getBooleanExtra("logout", false);
        if(logout) {
            Log.d(TAG, "logout:start");
            auth.signOut();
            Log.d(TAG, "logout:done");
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        Intent intent;
        if(currentUser == null) {
            intent = new Intent(this, LoginActivity.class);
        }
        else {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Log.d(TAG, "Starting activity: " + intent.toString());

        startActivity(intent);
        overridePendingTransition(0,0);
        finish();
    }

}
