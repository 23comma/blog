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
    public String list(Model model) {
        try {
            List<Post> posts = postService.findAll();
            model.addAttribute("posts", posts);
            model.addAttribute("tagRank", postService.getTagRank());
        } catch (Exception e) {
            model.addAttribute("error", "Database not found. Please create DB first.");
            model.addAttribute("posts", Collections.emptyList());
            model.addAttribute("tagRank", new LinkedHashMap<String, Integer>());
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
