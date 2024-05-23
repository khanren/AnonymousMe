package com.example.anonymousme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Category extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostRecyclerViewAdapter postAdapter;
    private List<Post> postList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private ImageButton myImageButton;
    private SearchView searchView;

    // Moved tempPostList to class level
    private List<Post> tempPostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostRecyclerViewAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String emailString = user.getEmail().replace(".", "-");
            firebaseDatabase = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
            databaseReference = firebaseDatabase.getReference();
            Query query = databaseReference.child("user").child(emailString);

            postAdapter.setOnItemClickListener(new PostRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Post clickedPost = postList.get(position);
                    Intent intent = new Intent(Category.this, PostDetailActivity.class);
                    intent.putExtra("postId", clickedPost.getPostId());
                    intent.putExtra("title", clickedPost.getTitle());
                    intent.putExtra("content", clickedPost.getContent());
                    intent.putStringArrayListExtra("imageUrls", new ArrayList<>(clickedPost.getImageUrls()));
                    startActivity(intent);
                }
            });
            fetchPostsFromDatabase();
        } else {
            Intent intent = new Intent(Category.this, Login.class);
            startActivity(intent);
            finish();
        }

        myImageButton = findViewById(R.id.myToolbar_user);
        myImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Category.this, UserProfile.class);
                startActivity(intent);
                finish();
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Post> filteredPostList = new ArrayList<>();
                for (Post post : postList) {
                    if (post.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                        filteredPostList.add(post);
                    }
                }
                postAdapter.filterList(filteredPostList);
                return false;
            }
        });
    }

    private void fetchPostsFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("posts");

        // Initialize the tempPostList variable
        tempPostList = new ArrayList<>();

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    processPost(postSnapshot);
                }

                updateTopPosts();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    processPost(postSnapshot);
                }

                updateTopPosts();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postId = postSnapshot.getKey();

                    // Replace the removeIf() method with an iterator
                    Iterator<Post> iterator = tempPostList.iterator();
                    while (iterator.hasNext()) {
                        Post post = iterator.next();
                        if (post.getPostId().equals(postId)) {
                            iterator.remove();
                        }
                    }
                }

                updateTopPosts();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        databaseRef.addChildEventListener(childEventListener);
    }

    private void processPost(DataSnapshot postSnapshot) {
        String postId = postSnapshot.getKey();
        String content = postSnapshot.child("content").getValue(String.class);
        String title = postSnapshot.child("title").getValue(String.class);
        int likes = 0; // default value
        if (postSnapshot.hasChild("likes")) {
            likes = postSnapshot.child("likes").getValue(Integer.class);
        }
        long timestamp = postSnapshot.child("timestamp").getValue(Long.class);
        List<String> imageUrls = new ArrayList<>();
        for (DataSnapshot imageUrlSnapshot : postSnapshot.child("imageUrls").getChildren()) {
            imageUrls.add(imageUrlSnapshot.getValue(String.class));
        }

        long currentTimeMillis = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;

        // Check if the post is within the last 24 hours
        if (currentTimeMillis - timestamp <= oneDayInMillis) {
            Post post = new Post(postId, title, content, imageUrls, likes, timestamp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tempPostList.removeIf(p -> p.getPostId().equals(postId));
            }
            tempPostList.add(post);
        }
    }

    private void updateTopPosts() {
        // Sort the tempPostList by the number of likes in descending order
        Collections.sort(tempPostList, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                return Integer.compare(p2.getLikes(), p1.getLikes());
            }
        });

        // Add the top 3 posts to the postList
        postList.clear();
        for (int i = 0; i < 3 && i < tempPostList.size(); i++) {
            postList.add(tempPostList.get(i));
        }

        postAdapter.notifyDataSetChanged();
    }
}
