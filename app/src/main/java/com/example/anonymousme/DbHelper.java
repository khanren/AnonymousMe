package com.example.anonymousme;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DbHelper {
    private DatabaseReference databaseReference;

    public DbHelper() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference("posts");
    }

    public void insertPost(Post post) {
        String postId = databaseReference.push().getKey();
        databaseReference.child(postId).setValue(post);
    }

    public void getAllPosts(final PostDataListener postDataListener) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Post> postList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                postDataListener.onDataReceived(postList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                postDataListener.onError(databaseError.getMessage());
            }
        });
    }

    public interface PostDataListener {
        void onDataReceived(List<Post> postList);
        void onError(String errorMessage);
    }
}
