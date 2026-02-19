package com.example.blog.controller;

import com.example.blog.entity.Post;
import com.example.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @GetMapping("/")
    public String list(
            @RequestParam(name = "tag", required = false) String tag,
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "sort", required = false, defaultValue = "created") String sortBy,
            @RequestParam(name = "order", required = false, defaultValue = "desc") String order,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {
        try {
            int pageSize = 10;
            List<Post> posts;
            int totalPages = 1;
            
            if (tag != null && !tag.isEmpty()) {
                posts = postService.findByTag(tag);
                model.addAttribute("searchTag", tag);
                model.addAttribute("searchKeyword", tag);
                totalPages = (int) Math.ceil((double) posts.size() / pageSize);
                int start = page * pageSize;
                int end = Math.min(start + pageSize, posts.size());
                if (start < posts.size()) {
                    posts = posts.subList(start, end);
                }
            } else if (keyword != null && !keyword.isEmpty()) {
                posts = postService.search(keyword);
                model.addAttribute("searchKeyword", keyword);
                totalPages = (int) Math.ceil((double) posts.size() / pageSize);
                int start = page * pageSize;
                int end = Math.min(start + pageSize, posts.size());
                if (start < posts.size()) {
                    posts = posts.subList(start, end);
                }
            } else {
                posts = postService.getSortedPosts(sortBy, order);
                totalPages = postService.getTotalPages(pageSize);
                List<Post> sortedPosts = posts;
                int start = page * pageSize;
                int end = Math.min(start + pageSize, sortedPosts.size());
                if (start < sortedPosts.size()) {
                    posts = sortedPosts.subList(start, end);
                }
            }
            
            model.addAttribute("posts", posts);
            model.addAttribute("tagRank", postService.getTagRank());
            model.addAttribute("viewRank", postService.getViewRank());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("order", order);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
        } catch (Exception e) {
            model.addAttribute("error", "Database not found. Please create DB first.");
            model.addAttribute("posts", Collections.emptyList());
            model.addAttribute("tagRank", new LinkedHashMap<String, Integer>());
            model.addAttribute("viewRank", Collections.emptyList());
        }
        return "index";
    }
    
    @GetMapping("/post/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        try {
            postService.incrementViewCount(id);
            model.addAttribute("post", postService.findById(id).orElse(null));
        } catch (Exception e) {
            model.addAttribute("error", "Database not found.");
        }
        return "detail";
    }
    
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("post", new Post());
        return "write";
    }
    
    @PostMapping("/write")
    public String write(@ModelAttribute Post post) {
        postService.save(post);
        return "redirect:/";
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("post", postService.findById(id).orElse(null));
        return "edit";
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, @ModelAttribute Post post) {
        post.setId(id);
        postService.save(post);
        return "redirect:/post/" + id;
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        postService.deleteById(id);
        return "redirect:/";
    }
}
