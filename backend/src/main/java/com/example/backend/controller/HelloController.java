package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //RESTful API controller
@RequestMapping("/api/test") // URL 경로

public class HelloController {

    @GetMapping("/hello") //GET
    public String hello(){
        return "Hello this is test Backend";
    }
}
