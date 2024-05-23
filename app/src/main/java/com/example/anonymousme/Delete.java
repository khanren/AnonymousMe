package com.example.anonymousme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Delete extends RecyclerView.Adapter<Delete.DeleteFun> {
    List<String> items;

    public Delete(List<String> items){
        this.items = items;
    }

    @NonNull
    @Override
    public DeleteFun onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new DeleteFun(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteFun holder, int position) {
        holder.textView.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class DeleteFun extends RecyclerView.ViewHolder {
        TextView textView;
        private Delete adapter;

        public DeleteFun(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.comment);
            itemView.findViewById(R.id.comment_deletebtn).setOnClickListener(view -> {
                adapter.items.remove(getAdapterPosition());
                adapter.notifyItemRemoved(getAdapterPosition());
            });
        }

        public DeleteFun linkAdapter(Delete adapter) {
            this.adapter = adapter;
            return this;
        }
    }
}

