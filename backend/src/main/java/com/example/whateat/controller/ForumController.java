package com.example.whateat.controller;

import com.example.whateat.common.Result;
import com.example.whateat.model.ForumComment;
import com.example.whateat.model.ForumPost;
import com.example.whateat.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/list")
    public Result<List<ForumPost>> getForumPosts() {
        return Result.success(forumService.getForumPosts());
    }

    @PostMapping("/create")
    public Result<ForumPost> createForumPost(@RequestBody ForumPost post) {
        return Result.success(forumService.createForumPost(post));
    }

    @PostMapping("/like/{postId}")
    public Result<ForumPost> likePost(@PathVariable Long postId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        try {
            return Result.success(forumService.likePost(postId, userId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{postId}")
    public Result<Void> deleteForumPost(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            forumService.deleteForumPost(postId, userId);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/comments/{postId}")
    public Result<List<ForumComment>> getComments(@PathVariable Long postId) {
        return Result.success(forumService.getComments(postId));
    }

    @PostMapping("/comment/add")
    public Result<ForumComment> addComment(@RequestBody ForumComment comment) {
        return Result.success(forumService.addComment(comment));
    }
}
