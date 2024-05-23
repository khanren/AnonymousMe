package com.example.anonymousme;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {
    private EditText username, email, edumail, password, password2;
    private Button mSignUpBtn;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("user");

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        edumail = findViewById(R.id.edumail);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        mSignUpBtn = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        // Set up the OnEditorActionListener for the email EditText
        username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    return true; // Consume the event to prevent going to the next line
                }
                return false; // Pass the event along
            }
        });

        // Set up the OnEditorActionListener for the email EditText
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    return true; // Consume the event to prevent going to the next line
                }
                return false; // Pass the event along
            }
        });

        // Set up the OnEditorActionListener for the email EditText
        edumail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    return true; // Consume the event to prevent going to the next line
                }
                return false; // Pass the event along
            }
        });

        // Set up the OnEditorActionListener for the password EditText
        password2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    return true; // Consume the event to prevent going to the next line
                }
                return false; // Pass the event along
            }
        });

        // Set up the OnEditorActionListener for the password EditText
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    return true; // Consume the event to prevent going to the next line
                }
                return false; // Pass the event along
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameString = username.getText().toString();
                String emailString = email.getText().toString().trim();
                String edumailString = edumail.getText().toString().trim();
                String passwordString = password.getText().toString().trim();
                String password2String = password2.getText().toString().trim();
                String isValidStudentEmail, isValidEmail;
                int minPasswordLength = 8;

                if (usernameString.isEmpty()) {
                    username.setError("Username cannot be empty");
                }
                if (emailString.isEmpty()) {
                    email.setError("Email cannot be empty");
                }
                if (edumailString.isEmpty()) {
                    edumail.setError("Email cannot be empty");
                }
                if (passwordString.isEmpty()) {
                    password.setError("Password cannot be empty");
                }
                if (password2String.isEmpty()) {
                    password2.setError("Password cannot be empty");
                }
                if (passwordString.length() < minPasswordLength) {
                    password.setError("Password must be at least " + minPasswordLength + " characters");
                } else if (!isValidEmail(email.getText().toString().trim())) {
                    email.setError("Please enter a valid email address");
                } else if (!passwordString.equals(password2String)) {
                    password2.setError("Passwords do not match");
                } else if (!isValidStudentEmail(edumail.getText().toString().trim())) {
                    edumail.setError("Please enter a valid student email address");
                } else {
                    checkIfEmailExists(emailString, edumailString, usernameString, passwordString);
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, Login.class));
            }
        });
    }

    private boolean isValidStudentEmail(String edumailString) {
        String emailPattern = "\\d{8}@imail\\.sunway\\.edu\\.my";
        String[] allowedDomains = {"imail.sunway.edu.my"};
        boolean isValidFormat = edumailString.matches(emailPattern);
        boolean isValidDomain = false;
        for (String domain : allowedDomains) {
            if (edumailString.endsWith("@" + domain)) {
                isValidDomain = true;
                break;
            }
        }
        return isValidFormat && isValidDomain;
    }

    private boolean isValidEmail(String email) {
        String[] allowedDomains = {"gmail.com", "yahoo.com"};
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        boolean isValidFormat = email.matches(emailPattern);
        boolean isValidDomain = false;
        for (String domain : allowedDomains) {
            if (email.endsWith("@" + domain)) {
                isValidDomain = true;
                break;
            }
        }
        return isValidFormat && isValidDomain;
    }

    private void checkIfEmailExists(final String emailString, final String edumailString, final String usernameString, final String passwordString) {
        Query emailQuery = mDatabase.orderByChild("Education Email").equalTo(edumailString);

        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Education email already exists
                    Toast.makeText(getApplicationContext(), "Education email already registered.", Toast.LENGTH_SHORT).show();
                } else {
                    // Education email does not exist, so create a new user
                    createUser(emailString, edumailString, usernameString, passwordString);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log any errors
                Log.e(TAG, "Error checking email: " + databaseError.getMessage());
            }
        });
    }


    private void createUser(String emailString, String edumailString, String usernameString, String passwordString) {
        mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            String key = emailString.replace(".", "-"); // Updated this line
                            DatabaseReference currentUserDb = mDatabase.child(key);
                            currentUserDb.child("Username").setValue(usernameString);
                            currentUserDb.child("Education Email").setValue(edumailString);
                            currentUserDb.child("Password").setValue(passwordString);
                            currentUserDb.child("User ID").setValue(userId);
                            // send verification email
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            Toast.makeText(getApplicationContext(), "Sign up successful. Please check your email to verify your account.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUp.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
