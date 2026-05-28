package com.example.whateat.controller;

import com.example.whateat.common.Result;
import com.example.whateat.model.LunchHistory;
import com.example.whateat.service.LunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private LunchService lunchService;

    @GetMapping("/list/{userId}")
    public Result<List<LunchHistory>> getHistoryList(@PathVariable Long userId) {
        return Result.success(lunchService.getHistoryList(userId));
    }
}
