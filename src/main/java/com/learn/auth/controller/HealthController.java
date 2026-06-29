package com.learn.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "🚀 Temp Backend is LIVE on Render!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}

