package com.example.anonymousme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {
    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        getSupportActionBar().hide();

        fullScreenImageView = findViewById(R.id.full_screen_image_view);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(fullScreenImageView);
        } else {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
