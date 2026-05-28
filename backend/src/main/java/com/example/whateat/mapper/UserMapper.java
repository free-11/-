package com.example.whateat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.whateat.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
