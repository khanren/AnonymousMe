package com.example.anonymousme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment extends AppCompatActivity {

    private RecyclerView commentRecyclerView;
    private EditText commentEditText;
    private ImageButton commentSendButton;
    private List<CommentModel> commentList;
    private CommentAdapter commentAdapter;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        getSupportActionBar().hide();

        postId = getIntent().getStringExtra("postId");

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://anonymous-me-63f82-default-rtdb.asia-southeast1.firebasedatabase.app");
        reference = database.getReference("Comments").child(postId);

        commentRecyclerView = findViewById(R.id.comment_recycler_view);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setHasFixedSize(true);
        commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(commentList, postId);
        commentRecyclerView.setAdapter(commentAdapter);

        commentEditText = findViewById(R.id.comment_edit_text);
        commentSendButton = findViewById(R.id.comment_send_button);

        commentSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = commentEditText.getText().toString();
                if (!TextUtils.isEmpty(commentText)) {
                    String currentUserId = mAuth.getCurrentUser().getUid();
                    String commentId = reference.push().getKey();

                    // Check for rude words and show AlertDialog to confirm replacement
                    if (commentText.matches("(?i).*fuck|shit|damn|fuck\\s+you|fuck|fucking|idiot|stupid|suck|dick|son\\s+of\\s+the\\s+bitch.*")) {
                        final String finalCommentText = commentText;
                        new AlertDialog.Builder(Comment.this)
                                .setTitle("Replace Rude Words")
                                .setMessage("Your comment contains rude words. Do you want to replace them with '?'")
                                .setPositiveButton("Replace", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String replacedCommentText = finalCommentText.replaceAll("(?i)fuck|shit|damn|fuck\\s+you|fuck|fucking|idiot|stupid|suck|dick|son\\s+of\\s+the\\s+bitch", "?");
                                        CommentModel comment = new CommentModel(replacedCommentText, currentUserId, commentId);
                                        reference.child(commentId).setValue(comment);
                                        commentEditText.setText("");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    } else {
                        CommentModel comment = new CommentModel(commentText, currentUserId, commentId);
                        reference.child(commentId).setValue(comment);
                        commentEditText.setText("");
                    }
                } else {
                    Toast.makeText(Comment.this, "Please write a comment.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentModel comment = snapshot.getValue(CommentModel.class);
                    if (comment != null) {
                        comment.setCommentId(snapshot.getKey());
                        commentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Comment.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class CommentModel {
        private String comment;
        private String uid;
        private String commentId;

        public CommentModel() {
        }

        public CommentModel(String comment, String uid, String commentId) {
            this.comment = comment;
            this.uid = uid;
            this.commentId = commentId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

    }
}