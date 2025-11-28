package com.agnostik.agnostik_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service

public class TextService {

    private final Map<Long, String> texts = new ConcurrentHashMap<>();

    public void updateText(Long userId, String text) {
        if (text == null) {
            text = "";
        }

        if (text.length() > 6000) {
            text = text.substring(0, 6000);
        }

        texts.put(userId, text);
    }

    public String getText(Long userId) {
        return texts.getOrDefault(userId, "");
    }

    public void clearText(Long userId) {
        texts.remove(userId);
    }
}
