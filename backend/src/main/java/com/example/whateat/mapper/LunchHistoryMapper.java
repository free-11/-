package com.example.whateat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.whateat.model.LunchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LunchHistoryMapper extends BaseMapper<LunchHistory> {

    @Select("SELECT * FROM lunch_history WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<LunchHistory> findByUserId(Long userId);
}
