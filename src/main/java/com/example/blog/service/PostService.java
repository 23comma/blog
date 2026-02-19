package com.example.blog.service;

import com.example.blog.entity.Post;
import com.example.blog.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS blog");
            stmt.execute("USE blog");
            stmt.execute("CREATE TABLE IF NOT EXISTS post (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "content TEXT, " +
                "author VARCHAR(255), " +
                "view_count INT DEFAULT 0, " +
                "tags VARCHAR(500), " +
                "created_at DATETIME, " +
                "updated_at DATETIME)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Post> findAll() {
        return postRepository.findAll();
    }
    
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }
    
    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }
    
    @Transactional
    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }
    
    @Transactional
    public void incrementViewCount(Long id) {
        Optional<Post> post = postRepository.findById(id);
        post.ifPresent(p -> {
            p.setViewCount(p.getViewCount() + 1);
            postRepository.save(p);
        });
    }
    
    public Map<String, Integer> getTagRank() {
        List<Post> posts = postRepository.findAll();
        Map<String, Integer> tagCount = new LinkedHashMap<>();
        
        for (Post post : posts) {
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                String[] tags = post.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCount.put(trimmedTag, tagCount.getOrDefault(trimmedTag, 0) + 1);
                    }
                }
            }
        }
        
        return tagCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}
