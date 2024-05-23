package com.example.anonymousme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment.CommentModel> commentList;
    private String postId;
    private String currentUserId;

    public CommentAdapter(List<Comment.CommentModel> commentList, String postId) {
        this.commentList = commentList;
        this.postId = postId;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getPostId() {
        return postId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        Log.d("CommentAdapter", "Inflated view: " + view);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment.CommentModel comment = commentList.get(position);
        holder.commentTextView.setText(comment.getComment());

        // Show/hide the delete button based on the current user's ID and the comment user's ID
        String commentUserId = comment.getUid();
        View deleteLayout = holder.itemView.findViewById(R.id.comment_deletebtn);
        if (currentUserId.equals(commentUserId)) {
            deleteLayout.setVisibility(View.VISIBLE);
        } else {
            deleteLayout.setVisibility(View.GONE);
        }

        // Set the click listener for the delete button
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Delete comment");
                builder.setMessage("Are you sure you want to delete this comment?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the comment
                        String commentId = commentList.get(holder.getAdapterPosition()).getCommentId();
                        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).child(commentId);
                        commentRef.removeValue();
                        removeComment(holder.getAdapterPosition());
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentTextView;
        ImageButton deleteButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment);
            deleteButton = itemView.findViewById(R.id.comment_deletebtn);
        }
    }

    // Add a new method to remove a comment and update the adapter
    public void removeComment(int position) {
        commentList.remove(position);
        notifyItemRemoved(position);
    }
}


class DeleteFun extends RecyclerView.ViewHolder {
    private ImageButton deleteButton;
    private CommentAdapter adapter;
    private String commentId;
    private int position;
    private String postId;

    public DeleteFun(@NonNull View itemView, CommentAdapter adapter, String commentId, int position, String postId) {
        super(itemView);
        this.adapter = adapter;
        this.commentId = commentId;
        this.position = position;
        this.postId = postId;
        deleteButton = itemView.findViewById(R.id.comment_deletebtn);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Delete comment");
                builder.setMessage("Are you sure you want to delete this comment?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the comment
                        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).child(commentId);
                        commentRef.removeValue();
                        adapter.removeComment(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    public DeleteFun linkAdapter(CommentAdapter adapter, String commentId, int position) {
        this.adapter = adapter;
        this.commentId = commentId;
        this.position = position;
        return this;
    }
}