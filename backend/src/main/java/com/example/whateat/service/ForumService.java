package com.example.whateat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.whateat.mapper.ForumCommentMapper;
import com.example.whateat.mapper.ForumPostMapper;
import com.example.whateat.model.ForumComment;
import com.example.whateat.model.ForumPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumService {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    public List<ForumPost> getForumPosts() {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ForumPost::getCreatedAt);
        return forumPostMapper.selectList(wrapper);
    }

    public ForumPost createForumPost(ForumPost post) {
        forumPostMapper.insert(post);
        return post;
    }

    public ForumPost likePost(Long postId, Long userId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        String likedUserIds = post.getLikedUserIds();
        String userIdStr = String.valueOf(userId);
        boolean alreadyLiked = likedUserIds != null && ("," + likedUserIds + ",").contains("," + userIdStr + ",");

        if (alreadyLiked) {
            likedUserIds = likedUserIds.replace(userIdStr + ",", "").replace("," + userIdStr, "");
            if (likedUserIds.startsWith(",")) likedUserIds = likedUserIds.substring(1);
            if (likedUserIds.endsWith(",")) likedUserIds = likedUserIds.substring(0, likedUserIds.length() - 1);
            post.setLikes(Math.max(0, (post.getLikes() != null ? post.getLikes() : 1) - 1));
        } else {
            likedUserIds = (likedUserIds != null ? likedUserIds + "," : "") + userIdStr;
            post.setLikes((post.getLikes() != null ? post.getLikes() : 0) + 1);
        }
        post.setLikedUserIds(likedUserIds);
        forumPostMapper.updateById(post);
        return post;
    }

    public void deleteForumPost(Long postId, Long userId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的帖子");
        }
        forumPostMapper.deleteById(postId);
    }

    public List<ForumComment> getComments(Long postId) {
        LambdaQueryWrapper<ForumComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumComment::getPostId, postId);
        wrapper.orderByAsc(ForumComment::getCreatedAt);
        return forumCommentMapper.selectList(wrapper);
    }

    public ForumComment addComment(ForumComment comment) {
        forumCommentMapper.insert(comment);
        return comment;
    }
}
