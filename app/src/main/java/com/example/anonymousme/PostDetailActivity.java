package com.example.anonymousme;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity implements ImageAdapter.OnImageClickListener {
    private TextView titleTextView;
    private TextView contentTextView;
    private RecyclerView recyclerView;
    private ArrayList<String> imageUrls;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private DatabaseReference likeRef;
    private DatabaseReference commentRef;
    private TextView likeCountTextView;
    private TextView commentCountTextView;
    private ImageView likeButton;
    private String postId;
    private String currentUserId;
    private ImageView commentButton;
    private String postUserId;
    private LinearLayout deleteLayout;
    private ImageView deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        getSupportActionBar().hide();

        // Get the post data from the intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        imageUrls = intent.getStringArrayListExtra("imageUrls");
        postId = intent.getStringExtra("postId");
        postUserId = intent.getStringExtra("userId");

        // Set up the views
        titleTextView = findViewById(R.id.post_title);
        contentTextView = findViewById(R.id.post_content);
        recyclerView = findViewById(R.id.image_recycler_view);
        likeCountTextView = findViewById(R.id.like_count);
        commentCountTextView = findViewById(R.id.comment_count);
        likeButton = findViewById(R.id.likebtn);
        commentButton = findViewById(R.id.commentbtn);
        deleteButton = findViewById(R.id.deletebtn);

        // Set the post data to the views
        titleTextView.setText(title);
        contentTextView.setText(content);

        // Convert imageUrls to selectedImageUris
        if (imageUrls != null && imageUrls.size() > 0) {
            for (String imageUrl : imageUrls) {
                selectedImageUris.add(Uri.parse(imageUrl));
            }
        } else {
            // Set a default image to the selectedImageUris list
            selectedImageUris.add(Uri.parse("android.resource://com.example.anonymousme/drawable/default_image"));
        }

        // Set up the RecyclerView with the ImageAdapter
        int imageSize = getResources().getDisplayMetrics().widthPixels / 2;
        ImageAdapter imageAdapter = new ImageAdapter(this, selectedImageUris, this, imageSize);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Set up Firebase and views
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app/");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        likeRef = database.getReference("likecount").child(postId);
        commentRef = database.getReference("Comments").child(postId);

        // Set up the like button click listener
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLikeButtonClick();
                return;
            }
        });

        // Load the initial state of the like button and like count
        loadLikeButtonAndCount();

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, Comment.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
                return;
            }
        });
        // Load the initial state of the comment count
        loadCommentCount();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
                return;
            }
        });

        loadPostUserId();
    }

    private void handleLikeButtonClick() {
        likeRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User has already liked the post, remove the like
                    for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                        likeSnapshot.getRef().removeValue();
                    }
                    likeButton.setImageResource(R.drawable.likebtn_baseline); // Change the like button image to the unliked state
                    Toast.makeText(PostDetailActivity.this, "Disliked", Toast.LENGTH_SHORT).show();
                } else {
                    // User hasn't liked the post yet, add a like
                    String likeId = likeRef.push().getKey();
                    likeRef.child(likeId).child("userId").setValue(currentUserId);
                    likeButton.setImageResource(R.drawable.likebtn); // Change the like button image to the liked state
                    Toast.makeText(PostDetailActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLikeButtonAndCount() {
        // Check if the user has already liked the post and set the like button accordingly
        likeRef.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    likeButton.setImageResource(R.drawable.likebtn); // Change the like button image to the liked state
                } else {
                    likeButton.setImageResource(R.drawable.likebtn_baseline); // Change the like button image to the unliked state
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Update the like count in realtime
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long likeCount = snapshot.getChildrenCount();
                String likeCountText = String.valueOf(likeCount) + " Likes";
                likeCountTextView.setText(likeCountText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCommentCount() {
        // Update the comment count in realtime
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                String commentCountText = String.valueOf(commentCount) + " Comments";
                commentCountTextView.setText(commentCountText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Delete the post, likes, comments, and images here
                        FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app/");
                        DatabaseReference postsRef = database.getReference("posts");
                        String modifiedEmail = postUserId.replace(".", "-");

                        // Retrieve the download URLs of the images from the database
                        DatabaseReference imagesRef = postsRef.child(modifiedEmail).child(postId).child("imageUrls");
                        imagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                List<String> imageUrls = new ArrayList<>();
                                for (DataSnapshot imageUrlSnapshot : dataSnapshot.getChildren()) {
                                    imageUrls.add(imageUrlSnapshot.getValue(String.class));
                                }

                                // Delete the post
                                postsRef.child(modifiedEmail).child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Post deleted successfully
                                            Toast.makeText(getApplicationContext(), "Post deleted successfully.", Toast.LENGTH_SHORT).show();

                                            // Delete the likes
                                            DatabaseReference likesRef = database.getReference("likecount").child(postId);
                                            likesRef.removeValue();

                                            // Delete the comments
                                            DatabaseReference commentsRef = database.getReference("Comments").child(postId);
                                            commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                                                        commentSnapshot.getRef().removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    // Handle error
                                                }
                                            });

                                            // Delete the images from Firebase Storage
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
                                            imagesRef.removeValue();

                                            finish();
                                        } else {
                                            // Failed to delete the post, handle the error here
                                            Toast.makeText(getApplicationContext(), "Failed to delete the post. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User canceled the dialog, do nothing
                    }
                });

        builder.create().show();
    }

    @Override
    public void onImageClick(Uri imageUri) {
        Intent intent = new Intent(PostDetailActivity.this, FullScreenImageActivity.class);
        intent.putExtra("imageUrl", imageUri.toString());
        startActivity(intent);
    }

    private void loadPostUserId() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference postsRef = database.getReference("posts");

        // Get the current user's email address
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = currentUser != null ? currentUser.getEmail() : null;

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean postFound = false;

                for (DataSnapshot emailSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot postSnapshot : emailSnapshot.getChildren()) {
                        if (postSnapshot.getKey().equals(postId)) {
                            postUserId = emailSnapshot.getKey().replace("-", ".");
                            postFound = true;
                            break;
                        }
                    }

                    if (postFound) {
                        break;
                    }
                }

                // Check if the delete button should be displayed
                if (currentUserEmail != null && currentUserEmail.equals(postUserId)) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else {
                    deleteButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}