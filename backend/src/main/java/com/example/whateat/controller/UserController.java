package com.example.whateat.controller;

import com.example.whateat.common.Result;
import com.example.whateat.model.User;
import com.example.whateat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> request) {
        String email = request.get("email") != null ? request.get("email").trim() : "";
        String password = request.get("password") != null ? request.get("password").trim() : "";
        String nickname = request.getOrDefault("nickname", "");
        System.out.println("register收到参数: email='" + email + "', password='" + password + "', nickname='" + nickname + "'");

        try {
            User user = userService.register(email, password, nickname);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> request) {
        String email = request.get("email") != null ? request.get("email").trim() : "";
        String password = request.get("password") != null ? request.get("password").trim() : "";
        System.out.println("login收到参数: email='" + email + "', password='" + password + "'");

        try {
            User user = userService.login(email, password);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update-nickname")
    public Result<User> updateNickname(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("userId"));
        String nickname = request.get("nickname");

        try {
            User user = userService.updateNickname(userId, nickname);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("userId"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
