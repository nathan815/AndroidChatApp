package com.learninga_z.myfirstapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.learninga_z.myfirstapp.AppConstants;
import com.learninga_z.myfirstapp.R;

public class CreateAccountActivity extends AppCompatActivity {
    private static final String TAG = "CreateAccountActivity";

    private boolean creatingAccount;

    private EditText emailView, passwordView, confirmPasswordView;
    private View progressView;
    private View registerFormView;

    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        fireAuth = FirebaseAuth.getInstance();

        emailView = findViewById(R.id.register_email);
        passwordView = findViewById(R.id.register_password);
        confirmPasswordView = findViewById(R.id.register_confirm_password);
        registerFormView = findViewById(R.id.register_form);
        progressView = findViewById(R.id.register_progress);

        Button createAccountButton = findViewById(R.id.register_action_button);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateAccount();
            }
        });

        Button signInButton = findViewById(R.id.go_to_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        Intent sourceIntent = getIntent();
        if(sourceIntent != null) {
            String prefilledEmail = sourceIntent.getStringExtra("EMAIL_ADDRESS");
            if(prefilledEmail != null) {
                emailView.setText(prefilledEmail);
            }
        }

    }

    private void attemptCreateAccount() {
        if (creatingAccount) {
            return;
        }

        Log.v(TAG, "register:begin");

        emailView.setError(null);
        passwordView.setError(null);
        confirmPasswordView.setError(null);

        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for confirm password
        if(TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = confirmPasswordView;
            cancel = true;
        }
        else if(!password.equals(confirmPassword)) {
            confirmPasswordView.setError(getString(R.string.error_confirm_password));
            focusView = confirmPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password, AppConstants.PASSWORD_MIN_LENGTH));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        showProgress(true);
        creatingAccount = true;

        fireAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        creatingAccount = false;

                        if (task.isSuccessful()) {
                            Log.v(TAG, "register:success");
                            registerSuccess();
                        }
                        else {
                            Log.v(TAG, "register:failure", task.getException());
                            registerError(task.getException());
                        }
                    }
                });
    }


    private void registerSuccess() {
        Toast toast = Toast.makeText(getApplicationContext(), "Created account successfully.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
        toast.show();

        Intent i = new Intent(CreateAccountActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void registerError(Exception e) {
        String error = getString(R.string.error_register_failed);
        if (e instanceof FirebaseAuthUserCollisionException) {
            error = getString(R.string.error_email_exists);
        }
        emailView.setError(error);
        emailView.requestFocus();
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= AppConstants.PASSWORD_MIN_LENGTH;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
