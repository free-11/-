package com.example.whateat.controller;

import com.example.whateat.common.Result;
import com.example.whateat.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/recommend")
    public Result<String> recommend(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.getOrDefault("userId", "0"));
        String message = request.getOrDefault("message", "今天吃什么？给我一些建议");

        try {
            String reply = aiService.recommend(userId, message);
            return Result.success(reply);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
