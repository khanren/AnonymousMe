package com.example.anonymousme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Post> postList;
    private PostAdapter postAdapter;
    private SearchView searchView;
    private List<Post> filteredPostList;

    @NonNull
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_card, parent, false);
        return new PostAdapter.PostViewHolder(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        // Update the onItemClick method to use the filteredPostList instead of the postList
        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Post clickedPost = filteredPostList.get(position);
                Intent intent = new Intent(Home.this, PostDetailActivity.class);
                intent.putExtra("postId", clickedPost.getPostId());
                intent.putExtra("title", clickedPost.getTitle());
                intent.putExtra("content", clickedPost.getContent());
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(clickedPost.getImageUrls()));
                startActivity(intent);
            }
        });

        fetchPostsFromDatabase();
        setUpSearchView();

        ImageButton myImageButton = findViewById(R.id.myToolbar_catagory);
        myImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Category.class);
                startActivity(intent);
                return;
            }
        });

        myImageButton = findViewById(R.id.myToolbar_user);
        myImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, UserProfile.class);
                startActivity(intent);
                return;
            }
        });

        FloatingActionButton fabPublishPost = findViewById(R.id.fabPublishPost);
        fabPublishPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, PublishPost.class);
                startActivity(intent);
                return;
            }
        });
    }

    private void setUpSearchView() {
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Not needed for this implementation
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the postList based on the user's search query
                List<Post> filteredList = new ArrayList<>();
                for (Post post : postList) {
                    if (post.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(post);
                    }
                }
                postAdapter.filterList(filteredList);
                filteredPostList = filteredList;
                return true;
            }
        });
    }

    private void fetchPostsFromDatabase () {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("posts");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot userEmailSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot postSnapshot : userEmailSnapshot.getChildren()) {
                        String postId = postSnapshot.getKey();
                        String content = postSnapshot.child("content").getValue(String.class);
                        String title = postSnapshot.child("title").getValue(String.class);
                        List<String> imageUrls = new ArrayList<>();
                        for (DataSnapshot imageUrlSnapshot : postSnapshot.child("imageUrls").getChildren()) {
                            imageUrls.add(imageUrlSnapshot.getValue(String.class));
                        }
                        Long timestamp = postSnapshot.child("timestamp").getValue(Long.class);

                        Post post = new Post(postId, title, content, imageUrls, timestamp);

                        // Check for duplicate content
                        boolean duplicate = false;
                        for (Post existingPost : postList) {
                            if (existingPost.getTitle().equals(post.getTitle()) && existingPost.getContent().equals(post.getContent())) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (!duplicate) {
                            postList.add(post);
                        }
                    }
                }

                // Sort the postList in descending order of timestamp
                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post post1, Post post2) {
                        Long timestamp1 = post1.getTimestamp();
                        Long timestamp2 = post2.getTimestamp();
                        return -timestamp1.compareTo(timestamp2);
                    }
                });
                postAdapter.notifyDataSetChanged(); // Notify the adapter that the data set has changed
                filteredPostList = new ArrayList<>(postList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

}