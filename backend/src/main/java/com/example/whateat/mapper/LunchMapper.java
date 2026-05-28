package com.example.whateat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.whateat.model.Lunch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LunchMapper extends BaseMapper<Lunch> {

    @Select("SELECT * FROM lunches WHERE user_id = #{userId}")
    List<Lunch> findByUserId(Long userId);
}
