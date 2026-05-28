package com.example.whateat.service;

import com.example.whateat.mapper.LunchHistoryMapper;
import com.example.whateat.mapper.LunchMapper;
import com.example.whateat.model.Lunch;
import com.example.whateat.model.LunchHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LunchService {

    @Autowired
    private LunchMapper lunchMapper;

    @Autowired
    private LunchHistoryMapper lunchHistoryMapper;

    public List<Lunch> getLunchList(Long userId) {
        return lunchMapper.findByUserId(userId);
    }

    public Lunch addLunch(Lunch lunch) {
        lunchMapper.insert(lunch);
        return lunch;
    }

    public Lunch updateLunch(Long id, Lunch lunch) {
        Lunch existing = lunchMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("午餐不存在");
        }
        existing.setName(lunch.getName());
        existing.setDescription(lunch.getDescription());
        existing.setTags(lunch.getTags());
        lunchMapper.updateById(existing);
        return existing;
    }

    public void deleteLunch(Long id) {
        lunchMapper.deleteById(id);
    }

    public Lunch spinLunch(Long userId, String tags) {
        List<Lunch> lunches = lunchMapper.findByUserId(userId);
        if (lunches.isEmpty()) {
            throw new RuntimeException("暂无午餐选项");
        }

        if (tags != null && !tags.isEmpty()) {
            Set<String> tagSet = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
            if (!tagSet.isEmpty()) {
                lunches = lunches.stream()
                    .filter(l -> l.getTags() != null && Arrays.stream(l.getTags().split(","))
                        .map(String::trim)
                        .anyMatch(tagSet::contains))
                    .collect(Collectors.toList());
            }
        }

        if (lunches.isEmpty()) {
            throw new RuntimeException("没有匹配的午餐选项");
        }

        Lunch selected = lunches.get(new Random().nextInt(lunches.size()));

        LunchHistory history = new LunchHistory();
        history.setUserId(userId);
        history.setLunchName(selected.getName());
        history.setLunchDescription(selected.getDescription());
        history.setTags(selected.getTags());
        lunchHistoryMapper.insert(history);

        return selected;
    }

    public List<LunchHistory> getHistoryList(Long userId) {
        return lunchHistoryMapper.findByUserId(userId);
    }

    public Map<String, Object> getStats(Long userId) {
        List<LunchHistory> histories = lunchHistoryMapper.findByUserId(userId);
        List<Lunch> lunches = lunchMapper.findByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSpins", histories.size());
        stats.put("totalDishes", lunches.size());

        Map<String, Long> frequencyMap = histories.stream()
            .collect(Collectors.groupingBy(LunchHistory::getLunchName, Collectors.counting()));

        List<Map<String, Object>> topDishes = frequencyMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", e.getKey());
                item.put("count", e.getValue());
                return item;
            })
            .collect(Collectors.toList());
        stats.put("topDishes", topDishes);

        Map<String, Long> dayOfWeekMap = histories.stream()
            .collect(Collectors.groupingBy(h -> {
                int day = h.getCreatedAt().getDayOfWeek().getValue();
                String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                return days[day];
            }, Collectors.counting()));
        stats.put("byDayOfWeek", dayOfWeekMap);

        return stats;
    }
}
