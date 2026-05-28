package com.example.whateat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.whateat.mapper.UserMapper;
import com.example.whateat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User register(String email, String password, String nickname) {
        User existing = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        if (existing != null) {
            throw new RuntimeException("邮箱已注册");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname.isEmpty() ? null : nickname);
        userMapper.insert(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    public User updateNickname(Long userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : null);
        userMapper.updateById(user);
        return user;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(newPassword);
        userMapper.updateById(user);
    }
}
