package com.example.blog.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccessException(DataAccessException e, Model model) {
        model.addAttribute("error", "Database is not available. Please create DB first.");
        model.addAttribute("posts", null);
        return "index";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("posts", null);
        return "index";
    }
}
