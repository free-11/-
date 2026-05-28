package com.example.whateat.controller;

import com.example.whateat.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/recommend/stream")
    public SseEmitter recommendStream(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.getOrDefault("userId", "0"));
        String message = request.getOrDefault("message", "今天吃什么？给我一些建议");
        return aiService.streamRecommend(userId, message);
    }
}
