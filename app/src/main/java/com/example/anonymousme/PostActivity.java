package com.example.anonymousme;

import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private List<Post> postList;
    private RecyclerView postRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getSupportActionBar().hide();

        postRecyclerView = findViewById(R.id.recyclerView);

        // Create a DatabaseReference object to reference the "posts" node in the Firebase database
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList = new ArrayList<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    String title = postSnapshot.child("title").getValue(String.class);
                    String content = postSnapshot.child("content").getValue(String.class);
                    ArrayList<String> imageUrls = new ArrayList<>();
                    for (DataSnapshot imageSnapshot : postSnapshot.child("imagesUrls").getChildren()) {
                        String imageUrl = imageSnapshot.getValue(String.class);
                        imageUrls.add(imageUrl);
                    }
                    Post post = new Post(postId, title, content, imageUrls);
                    postList.add(post);
                }

                // Create a new instance of the PostAdapter and set it on the RecyclerView
                PostAdapter postAdapter = new PostAdapter(PostActivity.this, postList);
                postRecyclerView.setAdapter(postAdapter);

                // Set an OnItemClickListener on the PostAdapter to start the PostDetailActivity when a post is clicked
                postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Post clickedPost = postList.get(position);
                        Intent intent = new Intent(PostActivity.this, PostDetailActivity.class);
                        intent.putExtra("title", clickedPost.getTitle());
                        intent.putExtra("content", clickedPost.getContent());
                        intent.putStringArrayListExtra("imageUrls", (ArrayList<String>) clickedPost.getImageUrls());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }
}
