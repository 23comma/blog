package com.example.blog.controller;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/db")
public class InitDBController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("")
    public String dbPage() {
        return "db";
    }
    
    @PostMapping("")
    public String createDB() {
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
        return "redirect:/";
    }
    
    @PostMapping("/sample")
    public String createSample() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("USE blog");
            stmt.execute("INSERT INTO post (title, content, author, view_count, tags, created_at, updated_at) VALUES " +
                "('첫 번째 글', '<h2>안녕하세요</h2><p>첫 번째 블로그 글입니다.</p>', 'Admin', 10, '인사, 첫글', NOW(), NOW()), " +
                "('두 번째 글', '<p>Spring Boot로 블로그를 만들었습니다.</p><ul><li>Thymeleaf</li><li>JPA</li><li>MariaDB</li></ul>', 'Admin', 5, 'Spring, Boot', NOW(), NOW()), " +
                "('세 번째 글', '<p>Summernote 에디터를 사용하고 있습니다.</p>', 'User1', 3, '에디터', NOW(), NOW())");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
    
    @PostMapping("/sample/many")
    public String createManySample() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("USE blog");
            
            String[] authors = {"Admin", "User1", "User2", "User3", "User4"};
            String[][] tagOptions = {
                {"Java", "Spring", "Boot"}, {"Python", "Django"}, {"React", "Vue", "Angular"},
                {"Database", "MySQL", "MariaDB"}, {"AWS", "Docker", "Kubernetes"}
            };
            
            StringBuilder sql = new StringBuilder("INSERT INTO post (title, content, author, view_count, tags, created_at, updated_at) VALUES ");
            
            for (int i = 1; i <= 123; i++) {
                String author = authors[i % authors.length];
                int views = (int)(Math.random() * 100);
                StringBuilder tags = new StringBuilder();
                String[] tagsArr = tagOptions[i % tagOptions.length];
                for (int j = 0; j < tagsArr.length; j++) {
                    if (j > 0) tags.append(", ");
                    tags.append(tagsArr[j]);
                }
                sql.append(String.format("('제목 %d', '<p>내용 %d입니다. 이것은 테스트 글입니다.</p>', '%s', %d, '%s', NOW(), NOW())", i, i, author, views, tags.toString()));
                if (i < 123) sql.append(", ");
            }
            
            stmt.execute(sql.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
    
    @DeleteMapping("/data")
    public String deleteData() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE 'blog'");
            if (rs.next()) {
                stmt.execute("USE blog");
                stmt.execute("DELETE FROM post");
            }
            rs.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
    
    @DeleteMapping("")
    public String dropDB() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE 'blog'");
            if (rs.next()) {
                stmt.execute("DROP DATABASE IF EXISTS blog");
            }
            rs.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
}
