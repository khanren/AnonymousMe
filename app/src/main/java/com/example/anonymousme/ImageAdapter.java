package com.example.anonymousme;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private ArrayList<Uri> mImageUris;
    private OnImageClickListener mListener;
    private int mImageSize;

    public interface OnImageClickListener {
        void onImageClick(Uri imageUri);
    }

    public ImageAdapter(Context context, ArrayList<Uri> imageUris, OnImageClickListener listener, int imageSize) {
        mContext = context;
        mImageUris = imageUris;
        mListener = listener;
        mImageSize = imageSize;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        RecyclerView recyclerView = (RecyclerView) parent;
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = mImageUris.get(position);

        Glide.with(mContext)
                .load(imageUri)
                .override(Target.SIZE_ORIGINAL)
                .centerInside()
                .into(holder.imageView);

        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.width = mImageSize;
        layoutParams.height = mImageSize;
        holder.imageView.setLayoutParams(layoutParams);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onImageClick(imageUri);
                return;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mImageUris == null) {
            return 0;
        }
        return mImageUris.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item_view);
        }
    }
}
