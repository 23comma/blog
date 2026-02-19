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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    public List<Post> findAll(int page, int size) {
        List<Post> all = postRepository.findAll();
        int start = page * size;
        int end = Math.min(start + size, all.size());
        if (start >= all.size()) return Collections.emptyList();
        return all.subList(start, end);
    }
    
    public int getTotalPages(int size) {
        int total = postRepository.findAll().size();
        return (int) Math.ceil((double) total / size);
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
    
    public List<Post> getViewRank() {
        return postRepository.findAll().stream()
            .sorted((p1, p2) -> p2.getViewCount().compareTo(p1.getViewCount()))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    public List<Post> findByTag(String tag) {
        List<Post> allPosts = postRepository.findAll();
        return allPosts.stream()
            .filter(post -> post.getTags() != null && post.getTags().contains(tag))
            .collect(Collectors.toList());
    }
    
    public List<Post> search(String keyword) {
        List<Post> allPosts = postRepository.findAll();
        String lowerKeyword = keyword.toLowerCase();
        return allPosts.stream()
            .filter(post -> 
                (post.getTitle() != null && post.getTitle().toLowerCase().contains(lowerKeyword)) ||
                (post.getContent() != null && post.getContent().toLowerCase().contains(lowerKeyword)) ||
                (post.getAuthor() != null && post.getAuthor().toLowerCase().contains(lowerKeyword)) ||
                (post.getTags() != null && post.getTags().toLowerCase().contains(lowerKeyword)))
            .collect(Collectors.toList());
    }
    
    public List<Post> getSortedPosts(String sortBy, String order) {
        List<Post> posts = postRepository.findAll();
        
        Comparator<Post> comparator;
        switch (sortBy) {
            case "updated":
                comparator = Comparator.comparing(Post::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "views":
                comparator = Comparator.comparing(Post::getViewCount, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            default:
                comparator = Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        
        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }
        
        return posts.stream().sorted(comparator).collect(Collectors.toList());
    }
    
    public String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return text;
        }
        String regex = "(?i)(" + Pattern.quote(keyword) + ")";
        return text.replaceAll(regex, "<mark>$1</mark>");
    }
    
    public String getContentSnippet(String content, String keyword, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        content = content.replaceAll("<[^>]*>", "");
        
        if (keyword == null || keyword.isEmpty()) {
            if (content.length() > maxLength) {
                return content.substring(0, maxLength) + "...";
            }
            return content;
        }
        
        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int index = lowerContent.indexOf(lowerKeyword);
        
        if (index == -1) {
            if (content.length() > maxLength) {
                return content.substring(0, maxLength) + "...";
            }
            return content;
        }
        
        int start = Math.max(0, index - 50);
        int end = Math.min(content.length(), index + keyword.length() + 50);
        
        String snippet = content.substring(start, end);
        
        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        
        return highlightKeyword(snippet, keyword);
    }
}
