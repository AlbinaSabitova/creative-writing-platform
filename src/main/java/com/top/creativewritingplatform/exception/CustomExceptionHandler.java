package com.top.creativewritingplatform.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExists(UserAlreadyExistsException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "auth/register";
    }

    @ExceptionHandler(TextNotFoundException.class)
    public String handleTextNotFound(TextNotFoundException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/404";
    }
}