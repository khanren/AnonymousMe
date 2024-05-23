package com.example.anonymousme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView usernameTextView;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Button editProfileButton;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        String emailString = user.getEmail().replace(".", "-");
        firebaseDatabase = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();
        Query query = databaseReference.child("user").child(emailString);

        usernameTextView = findViewById(R.id.usernameTextView);
        editProfileButton = findViewById(R.id.editProfile);
        Button deleteAccountButton = findViewById(R.id.DltAccountBtn);
        ImageButton logoutButton = findViewById(R.id.logoutbtn);


        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the deleteAccount method to delete the user's account
                deleteAccount();
                return;
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserProfile.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("Username").getValue(String.class);
                    if (username != null) {
                        usernameTextView.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                usernameTextView.setText("Error: " + error.getMessage());
            }
        });

        // Set the OnItemClickListener for the PostAdapter
        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Post clickedPost = postList.get(position);
                Intent intent = new Intent(UserProfile.this, PostDetailActivity.class);
                intent.putExtra("postId", clickedPost.getPostId());
                intent.putExtra("title", clickedPost.getTitle());
                intent.putExtra("content", clickedPost.getContent());
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(clickedPost.getImageUrls()));
                startActivity(intent);
            }
        });
        fetchPostsFromDatabase();

        ImageButton myImageButton = findViewById(R.id.myToolbar_catagory);
        myImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, Category.class);
                startActivity(intent);
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, EditUserProfile.class);
                startActivity(intent);
            }
        });
    }

    private void fetchPostsFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("posts");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                String emailString = user.getEmail().replace(".", "-");
                for (DataSnapshot userEmailSnapshot : dataSnapshot.getChildren()) {
                    if (userEmailSnapshot.getKey().equals(emailString)) {
                        for (DataSnapshot postSnapshot : userEmailSnapshot.getChildren()) {
                            String postId = postSnapshot.getKey();
                            String content = postSnapshot.child("content").getValue(String.class);
                            String title = postSnapshot.child("title").getValue(String.class);
                            List<String> imageUrls = new ArrayList<>();
                            for (DataSnapshot imageUrlSnapshot : postSnapshot.child("imageUrls").getChildren()) {
                                imageUrls.add(imageUrlSnapshot.getValue(String.class));
                            }

                            Post post = new Post(postId, title, content, imageUrls);
                            postList.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged(); // Notify the adapter that the data set has changed
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify your identity");
        builder.setMessage("Please enter your password to delete your account:");

        // Create password EditText
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEditText.setPadding(32, 16, 32, 16);
        passwordEditText.setHint("Password");
        builder.setView(passwordEditText);

        builder.setPositiveButton("Delete Account", null); // Set to null initially
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            // Do nothing
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Disable the delete button until the user enters a password
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        // Add a TextChangedListener to enable/disable the delete button based on the password field
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Set the onClickListener for the delete button after it has been initialized
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To check the password entered same with the user's password or not
                String password = passwordEditText.getText().toString();

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();

                if (user == null) {
                    Toast.makeText(getApplicationContext(), "Error: Unable to get the current user.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Delete all posts, likes, and comments associated with the user
                            deleteAllPostsAndAssociatedData();

                            // Delete the user account
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        Intent intent = new Intent(UserProfile.this, Login.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        dialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to reauthenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }






    private void deleteAllPostsAndAssociatedData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getApplicationContext(), "Error: Unable to get the current user.", Toast.LENGTH_SHORT).show();
            return;
        }

        String modifiedEmail = user.getEmail().replace(".", "-");

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // Query the database for the comments and likes made by the user on other user's posts
        Query commentsQuery = database.getReference("Comments").orderByChild("uid").equalTo(modifiedEmail);
        Query likesQuery = database.getReference("likecount").orderByChild("userId").equalTo(modifiedEmail);

        // Check if the user has made any comments or likes on other users' posts, and delete them first
        commentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("DeleteAccount", "Number of comments: " + dataSnapshot.getChildrenCount());
                if (dataSnapshot.exists()) {
                    for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                        String postId = commentSnapshot.child("postId").getValue(String.class);

                        // Check if the comment was made on the user's own post
                        if (!postId.startsWith(modifiedEmail)) {
                            commentSnapshot.getRef().removeValue();
                        }
                    }

                    // Delete the likes made by the user on other user's posts
                    likesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("DeleteAccount", "Number of likes: " + dataSnapshot.getChildrenCount());
                            for (DataSnapshot likeSnapshot : dataSnapshot.getChildren()) {
                                String postId = likeSnapshot.getKey();

                                // Check if the like was made on the user's own post
                                if (!postId.startsWith(modifiedEmail)) {
                                    likeSnapshot.getRef().removeValue();
                                }
                            }

                            // After deleting the user's comments and likes on other users' posts,
                            // delete the user's own posts and associated data
                            deleteSelfPostsAndAssociatedData(database, modifiedEmail);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                } else {
                    // If the user has not made any comments on other users' posts, check if they have made any likes
                    likesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot likeSnapshot : dataSnapshot.getChildren()) {
                                    String postId = likeSnapshot.getKey();

                                    // Check if the like was made on the user's own post
                                    if (!postId.startsWith(modifiedEmail)) {
                                        likeSnapshot.getRef().removeValue();
                                    }
                                }
                            }

                            // After deleting the user's likes on other users' posts,
                            // delete the user's own posts and associated data
                            deleteSelfPostsAndAssociatedData(database, modifiedEmail);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    private void deleteSelfPostsAndAssociatedData(FirebaseDatabase database, String modifiedEmail) {
        // Delete the user's Firebase Storage images from their own posts
        DatabaseReference selfPostsRef = database.getReference("posts").child(modifiedEmail);
        selfPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    // Retrieve the download URLs of the images from the database
                    DataSnapshot imagesSnapshot = postSnapshot.child("imageUrls");
                    List<String> imageUrls = new ArrayList<>();
                    for (DataSnapshot imageUrlSnapshot : imagesSnapshot.getChildren()) {
                        imageUrls.add(imageUrlSnapshot.getValue(String.class));
                    }

                    // Delete images from Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    for (String imageUrl : imageUrls) {
                        StorageReference imageRef = storageRef.getStorage().getReferenceFromUrl(imageUrl);
                        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Image deleted successfully
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to delete the image, handle the error here
                            }
                        });
                    }

                    // Remove the URLs of the images from the database
                    postSnapshot.getRef().child("imageUrls").removeValue();

                    // Delete comments made on the post
                    DatabaseReference commentsRef = database.getReference("Comments").child(postId);
                    commentsRef.removeValue();

                    // Delete likes made on the post
                    DatabaseReference likesRef = database.getReference("likecount").child(postId);
                    likesRef.removeValue();
                }

                // Delete the user's posts
                selfPostsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "All posts and associated data deleted successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to delete all posts and associated data. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Delete the user's comments
                DatabaseReference selfCommentsRef = database.getReference("Comments");
                selfCommentsRef.orderByChild("userId").equalTo(modifiedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                            commentSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });

                // Delete the likes made on the user's posts
                DatabaseReference userLikesRef = database.getReference("likecount");
                userLikesRef.orderByChild("userId").equalTo(modifiedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot likeSnapshot : dataSnapshot.getChildren()) {
                            likeSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });

                // Delete the comments made on the user's posts
                DatabaseReference userCommentsRef = database.getReference("Comments");
                userCommentsRef.orderByChild("userId").equalTo(modifiedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                            commentSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });

                // Delete the user's details
                DatabaseReference userRef = database.getReference("user").child(modifiedEmail);
                userRef.removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}