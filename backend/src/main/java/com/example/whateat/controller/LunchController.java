package com.example.whateat.controller;

import com.example.whateat.common.Result;
import com.example.whateat.model.Lunch;
import com.example.whateat.service.LunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lunch")
public class LunchController {

    @Autowired
    private LunchService lunchService;

    @GetMapping("/list/{userId}")
    public Result<List<Lunch>> getLunchList(@PathVariable Long userId) {
        return Result.success(lunchService.getLunchList(userId));
    }

    @PostMapping("/add")
    public Result<Lunch> addLunch(@RequestBody Lunch lunch) {
        return Result.success(lunchService.addLunch(lunch));
    }

    @PutMapping("/update/{id}")
    public Result<Lunch> updateLunch(@PathVariable Long id, @RequestBody Lunch lunch) {
        try {
            return Result.success(lunchService.updateLunch(id, lunch));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteLunch(@PathVariable Long id) {
        lunchService.deleteLunch(id);
        return Result.success(null);
    }

    @GetMapping("/spin/{userId}")
    public Result<Lunch> spinLunch(@PathVariable Long userId, @RequestParam(required = false) String tags) {
        try {
            return Result.success(lunchService.spinLunch(userId, tags));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
