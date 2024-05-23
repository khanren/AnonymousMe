package com.example.anonymousme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Login extends AppCompatActivity {
    private TextView textView, tvSignUp;
    private EditText email, password;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            private boolean isButtonClickable = true;

            public void onClick(View v) {
                if (isButtonClickable) {
                    isButtonClickable = false;

                    String mail = email.getText().toString();
                    String pass = password.getText().toString();
                    if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                        Toast.makeText(Login.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    } else if (pass.isEmpty()) {
                        Toast.makeText(Login.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    } else {
                        // Call the signInWithEmailAndPassword method
                        mAuth.signInWithEmailAndPassword(mail, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null && user.isEmailVerified()) {
                                            String email = user.getEmail();
                                            String uid = user.getUid();

                                            HashMap<Object, String> hashMap = new HashMap<>();
                                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
                                            DatabaseReference reference = database.getReference("user");
                                            reference.child(uid).setValue(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            startActivity(new Intent(Login.this, Home.class));
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(Login.this, "Data could not be added: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(Login.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut(); // Sign out the user
                                        }
                                    }

                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Authentication failed, show error message
                                        Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    // Reset the flag after a delay (e.g., 1 second)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isButtonClickable = true;
                        }
                    }, 5000); // 1000 milliseconds = 1 second
                }
                return;
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                return;
            }
        });

        textView = (TextView) findViewById(R.id.tvForgotPassword);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
                return;
            }
        });
    }
}

