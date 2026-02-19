package com.example.blog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String author;
    
    private Integer viewCount = 0;
    
    private String tags;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getContentSnippet(int maxLength) {
        if (content == null || content.isEmpty()) return "";
        String plainText = content.replaceAll("<[^>]*>", "");
        if (plainText.length() <= maxLength) return plainText;
        return plainText.substring(0, maxLength) + "...";
    }
    
    public String getHighlightedContent(String keyword) {
        if (content == null || keyword == null || keyword.isEmpty()) {
            return getContentSnippet(200);
        }
        String plainText = content.replaceAll("<[^>]*>", "");
        String regex = "(?i)(" + keyword + ")";
        String result = plainText.replaceAll(regex, "<mark>$1</mark>");
        int index = result.toLowerCase().indexOf("<mark>");
        if (index == -1) return getContentSnippet(200);
        int start = Math.max(0, index - 50);
        int end = Math.min(result.length(), index + keyword.length() + 100);
        String snippet = result.substring(start, end);
        return (start > 0 ? "..." : "") + snippet + (end < result.length() ? "..." : "");
    }
}
