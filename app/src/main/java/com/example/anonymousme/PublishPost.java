package com.example.anonymousme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublishPost extends AppCompatActivity {
    private EditText postTitleEditText;
    private EditText postContentEditText;
    private Button publishPostButton;
    private DatabaseReference postsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String currentUserEmail;
    private static final int PICK_IMAGE_REQUEST = 1;
    private GridLayout imageGrid;
    private Button selectImageButton;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private StorageReference storageReference;
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post);
        getSupportActionBar().hide();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            currentUserEmail = currentUser.getEmail();
        } else {
            // Redirect to login or show an error message
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageGrid = findViewById(R.id.ImageGrid);
        selectImageButton = findViewById(R.id.selectImageButton);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                return;
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
        postsRef = database.getReference("posts");

        postTitleEditText = findViewById(R.id.postTitleEditText);
        postContentEditText = findViewById(R.id.postContentEditText);
        publishPostButton = findViewById(R.id.publishPostButton);

        postTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String postTitle = postTitleEditText.getText().toString().trim();
                if (postTitle.split("\\s+").length > 15) {
                    publishPostButton.setEnabled(false);
                    Toast.makeText(PublishPost.this, "Maximum 15 words allowed in the title.", Toast.LENGTH_SHORT).show();
                } else {
                    publishPostButton.setEnabled(true);
                }
            }
        });

        postContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String postTitle = postTitleEditText.getText().toString().trim();
                String postContent = postContentEditText.getText().toString().trim();
                if (postContent.split("\\s+").length > 250) {
                    publishPostButton.setEnabled(false);
                    Toast.makeText(PublishPost.this, "Maximum 250 words allowed in the post content.", Toast.LENGTH_SHORT).show();
                } else if (postTitle.split("\\s+").length > 15) {
                    publishPostButton.setEnabled(false);
                    Toast.makeText(PublishPost.this, "Maximum 15 words allowed in the title.", Toast.LENGTH_SHORT).show();
                } else {
                    publishPostButton.setEnabled(true);
                }
            }
        });

        publishPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postTitle = postTitleEditText.getText().toString().trim();
                String postContent = postContentEditText.getText().toString().trim();

                if (postTitle.isEmpty() || postContent.isEmpty()) {
                    Toast.makeText(PublishPost.this, "Please fill in both the title and content.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check for rude words in the post title
                if (postTitle.matches("(?i).*fuck.*|.*shit.*|.*damn.*|.*idiot.*|.*stupid.*|.*suck.*|.*dick.*")) {
                    Toast.makeText(PublishPost.this, "Sorry, the post title contains a rude word. Please change the title and try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Replace the specific words with '?'
                String filteredPostContent = postContent.replaceAll("(?i)fuck|shit|damn|fuck\\s+you|fuck|fucking|idiot|stupid|suck|dick|son\\s+of\\s+the\\s+bitch", "?");

                if (!filteredPostContent.equals(postContent)) {
                    // Create an AlertDialog to confirm with the user
                    AlertDialog.Builder builder = new AlertDialog.Builder(PublishPost.this);
                    builder.setMessage("We found rude words in your post. Are you sure you want to post? If yes then the word will be replaced with '?'");
                    String finalPostTitle = postTitle;
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Post publishing logic
                            Post newPost = new Post(finalPostTitle, filteredPostContent, new ArrayList<>());
                            newPost.setTimestamp(System.currentTimeMillis()); // Add this line to set the timestamp
                            storePostInFirebase(newPost, currentUserEmail);
                            Toast.makeText(PublishPost.this, "Post published successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PublishPost.this, Home.class));
                        }
                    });
                    builder.setNegativeButton("No", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    // No rude words found, publish the post directly
                    Post newPost = new Post(postTitle, postContent, new ArrayList<>());
                    newPost.setTimestamp(System.currentTimeMillis()); // Add this line to set the timestamp
                    storePostInFirebase(newPost, currentUserEmail);
                    Toast.makeText(PublishPost.this, "Post published successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PublishPost.this, Home.class));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        String postTitle = postTitleEditText.getText().toString().trim();
        String postContent = postContentEditText.getText().toString().trim();

        if (!postTitle.isEmpty() || !postContent.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Discard Changes?");
            builder.setMessage("Your post has not been published. If you quit now, your changes will be lost. Are you sure you want to discard the changes?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("No", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    private void storePostInFirebase(Post post, String email) {
        String modifiedEmail = email.replace(".", "-");
        String key = postsRef.child(modifiedEmail).push().getKey();
        postsRef.child(modifiedEmail).child(key).setValue(post);

        List<String> imageUrls = new ArrayList<>();
        for (Uri imageUri : selectedImageUris) {
            StorageReference imageRef = storageReference.child(key + "/" + imageUri.getLastPathSegment());

            // Upload the image to Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save the download URL to the imageUrls list
                            imageUrls.add(uri.toString());

                            // Check if all images have been uploaded and URLs saved
                            if (imageUrls.size() == selectedImageUris.size()) {
                                // Save the imageUrls list to the Firebase Realtime Database
                                postsRef.child(modifiedEmail).child(key).child("imageUrls").setValue(imageUrls)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(PublishPost.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PublishPost.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int itemCount = data.getClipData().getItemCount();
                if (itemCount > 1) {
                    Toast.makeText(this, "You can only select up to 1 images", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedImageUris.clear();
                imageGrid.removeAllViews();

                for (int i = 0; i < itemCount; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bitmap);
                        imageView.setAdjustViewBounds(true);
                        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                        layoutParams.width = getResources().getDisplayMetrics().widthPixels / 4;
                        layoutParams.height = layoutParams.width;
                        layoutParams.setMargins(5, 5, 5, 5);
                        imageView.setLayoutParams(layoutParams);
                        imageGrid.addView(imageView);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(PublishPost.this, "Failed to load the image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (data.getData() != null) {
                // If the user selected only one image
                Uri imageUri = data.getData();
                selectedImageUris.clear();
                imageGrid.removeAllViews();
                selectedImageUris.add(imageUri);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(bitmap);
                    imageView.setAdjustViewBounds(true);
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.width = getResources().getDisplayMetrics().widthPixels / 3;
                    layoutParams.height = layoutParams.width;
                    layoutParams.setMargins(5, 5, 5, 5);
                    imageView.setLayoutParams(layoutParams);
                    imageGrid.addView(imageView);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PublishPost.this, "Failed to load the image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}