package com.example.whateat.service;

import com.example.whateat.mapper.LunchHistoryMapper;
import com.example.whateat.mapper.LunchMapper;
import com.example.whateat.model.Lunch;
import com.example.whateat.model.LunchHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class AiService {

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Autowired
    private LunchMapper lunchMapper;

    @Autowired
    private LunchHistoryMapper lunchHistoryMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    public String buildSystemPrompt(Long userId) {
        List<Lunch> lunchList = lunchMapper.findByUserId(userId);
        List<LunchHistory> historyList = lunchHistoryMapper.findByUserId(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的美食推荐助手，叫「美食小助手」。你的任务是根据用户的菜品库和饮食历史，给出个性化的美食推荐建议。\n\n");
        sb.append("## 用户菜品库\n");
        if (lunchList.isEmpty()) {
            sb.append("（用户还没有添加任何菜品）\n");
        } else {
            for (Lunch lunch : lunchList) {
                sb.append("- ").append(lunch.getName());
                if (lunch.getDescription() != null && !lunch.getDescription().isEmpty()) {
                    sb.append("（").append(lunch.getDescription()).append("）");
                }
                if (lunch.getTags() != null && !lunch.getTags().isEmpty()) {
                    sb.append(" [标签：").append(lunch.getTags()).append("]");
                }
                sb.append("\n");
            }
        }

        sb.append("\n## 最近饮食记录\n");
        if (historyList.isEmpty()) {
            sb.append("（暂无记录）\n");
        } else {
            historyList.stream()
                .limit(10)
                .forEach(h -> sb.append("- ").append(h.getLunchName()).append("\n"));
        }

        sb.append("\n## 回复要求\n");
        sb.append("1. 用中文回复，语气友好自然\n");
        sb.append("2. 根据用户的问题和其菜品库/历史给出有针对性的推荐\n");
        sb.append("3. 如果用户没有明确问题，可以主动根据历史记录分析饮食习惯并给出建议\n");
        sb.append("4. 回复控制在200字以内，简洁实用\n");
        sb.append("5. 可以适当使用emoji增加趣味性\n");

        return sb.toString();
    }

    public SseEmitter streamRecommend(Long userId, String userMessage) {
        SseEmitter emitter = new SseEmitter(120_000L);

        executor.execute(() -> {
            try {
                if (apiKey == null || apiKey.isEmpty()) {
                    emitter.send(SseEmitter.event().name("error").data("未配置 DeepSeek API Key，请在环境变量中设置 DEEPSEEK_API_KEY"));
                    emitter.complete();
                    return;
                }

                String systemPrompt = buildSystemPrompt(userId);

                Map<String, Object> bodyMap = new LinkedHashMap<>();
                bodyMap.put("model", model);
                bodyMap.put("temperature", 0.7);
                bodyMap.put("max_tokens", 500);
                bodyMap.put("stream", true);

                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> sysMsg = new LinkedHashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", systemPrompt);
                messages.add(sysMsg);

                Map<String, String> userMsg = new LinkedHashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                messages.add(userMsg);

                bodyMap.put("messages", messages);

                String jsonBody = toJsonString(bodyMap);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.deepseek.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(90))
                    .build();

                HttpResponse<java.io.InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
                );

                if (response.statusCode() != 200) {
                    String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                    emitter.send(SseEmitter.event().name("error").data(errorBody));
                    emitter.complete();
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                String line;
                StringBuilder fullContent = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("data:")) continue;

                    String data = line.substring(5).trim();

                    if ("[DONE]".equals(data)) {
                        emitter.send(SseEmitter.event().name("done").data(fullContent.toString()));
                        emitter.complete();
                        return;
                    }

                    try {
                        Map chunk = parseJson(data);
                        List choices = (List) chunk.get("choices");
                        if (choices == null || choices.isEmpty()) continue;

                        Map choice = (Map) choices.get(0);
                        Map delta = (Map) choice.get("delta");
                        if (delta == null) continue;

                        Object contentObj = delta.get("content");
                        if (contentObj != null && !contentObj.toString().isEmpty()) {
                            fullContent.append(contentObj.toString());
                            emitter.send(SseEmitter.event().name("chunk").data(contentObj.toString()));
                        }
                    } catch (Exception ignored) {}
                }

                emitter.send(SseEmitter.event().name("done").data(fullContent.toString()));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (Exception ignored) {}
            }
        });

        return emitter;
    }

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

    private static String toJsonString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            appendValue(sb, entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    private static void appendValue(StringBuilder sb, Object value) {
        if (value instanceof String) {
            sb.append("\"").append(escapeJson((String) value)).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof List) {
            sb.append("[");
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                appendValue(sb, list.get(i));
            }
            sb.append("]");
        } else if (value instanceof Map) {
            sb.append(toJsonStringRaw((Map) value));
        } else {
            sb.append("\"").append(value).append("\"");
        }
    }

    private static String toJsonStringRaw(Map map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Object keyObj : map.keySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(String.valueOf(keyObj))).append("\":");
            appendValue(sb, map.get(keyObj));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static Map parseJson(String json) {
        return new SimpleJsonParser(json).parseObject();
    }

    private static class SimpleJsonParser {
        private final String json;
        private int pos;

        SimpleJsonParser(String json) {
            this.json = json.trim();
            this.pos = 0;
        }

        Map<String, Object> parseObject() {
            skipWhitespace();
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() != '}') {
                do {
                    skipWhitespace();
                    String key = parseString();
                    skipWhitespace();
                    expect(':');
                    skipWhitespace();
                    Object val = parseValue();
                    map.put(key, val);
                    skipWhitespace();
                } while (accept(','));
            }
            expect('}');
            return map;
        }

        Object parseValue() {
            skipWhitespace();
            char c = peek();
            if (c == '"') return parseString();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') { consumeNull(); return null; }
            return parseNumber();
        }

        List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek() != ']') {
                do {
                    skipWhitespace();
                    list.add(parseValue());
                    skipWhitespace();
                } while (accept(','));
            }
            expect(']');
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '"') break;
                if (c == '\\') {
                    char esc = json.charAt(pos++);
                    switch (esc) {
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case '\\': sb.append('\\'); break;
                        case '"': sb.append('"'); break;
                        default: sb.append(esc); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Boolean parseBoolean() {
            if (json.startsWith("true", pos)) { pos += 4; return true; }
            if (json.startsWith("false", pos)) { pos += 5; return false; }
            throw new RuntimeException("Unexpected at pos " + pos);
        }

        void consumeNull() {
            if (!json.startsWith("null", pos)) throw new RuntimeException("Unexpected at pos " + pos);
            pos += 4;
        }

        Number parseNumber() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
            if (pos < json.length() && json.charAt(pos) == '.') {
                pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
            }
            String numStr = json.substring(start, pos);
            if (numStr.contains(".")) return Double.parseDouble(numStr);
            return Long.parseLong(numStr);
        }

        char peek() { return pos < json.length() ? json.charAt(pos) : '\0'; }

        void expect(char c) {
            if (pos < json.length() && json.charAt(pos) == c) { pos++; return; }
            throw new RuntimeException("Expected '" + c + "' at pos " + pos);
        }

        boolean accept(char c) {
            if (pos < json.length() && json.charAt(pos) == c) { pos++; return true; }
            return false;
        }

        void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
        }
    }
}
