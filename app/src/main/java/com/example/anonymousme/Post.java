package com.example.anonymousme;

import java.util.List;

public class Post {
    private String postId;
    private String content;
    private String title;
    private List<String> imageUrls;
    private int likes;
    private long timestamp;

    public Post(String title, String content, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
    }

    public Post(String postId, String title, String content, List<String> imageUrls) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
    }

    public Post(String postId, String title, String content, List<String> imageUrls, long timestamp) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.timestamp = timestamp;
    }

    public Post(String postId, String title, String content, List<String> imageUrls, int likes, long timestamp) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.likes = likes;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    // Add the getId() method
    public String getId() {
        return postId;
    }
}
