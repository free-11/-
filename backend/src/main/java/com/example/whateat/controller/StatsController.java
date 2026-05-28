package com.example.whateat.controller;

import com.example.whateat.common.Result;
import com.example.whateat.service.LunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private LunchService lunchService;

    @GetMapping("/{userId}")
    public Result<Map<String, Object>> getStats(@PathVariable Long userId) {
        return Result.success(lunchService.getStats(userId));
    }
}
