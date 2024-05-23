package com.example.anonymousme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditUserProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText usernameEditText;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
        String emailString = user.getEmail().replace(".", "-");
        databaseReference = firebaseDatabase.getReference().child("user").child(emailString);

        usernameEditText = findViewById(R.id.password);
        Button saveButton = findViewById(R.id.btnDone);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = usernameEditText.getText().toString();
                if (!newUsername.isEmpty()) {
                    updateUsername(newUsername);
                }
                return;
            }
        });
    }
    private void updateUsername(String newUsername) {
        databaseReference.child("Username").setValue(newUsername).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Navigate back to UserProfile activity
                Intent intent = new Intent(EditUserProfile.this, UserProfile.class);
                startActivity(intent);
                finish();
            } else {
                // Show error message
                usernameEditText.setError("Error updating username");
            }
        });
    }
}
