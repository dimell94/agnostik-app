package com.agnostik.agnostik_app.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NeighborTextStoreService {

    private final Map<Long, String> texts = new ConcurrentHashMap<>();
    private final int maxLength = 2000;

    public void setText(Long userId, String text){
        if (userId == null) return;
        String safe = text == null ? "" : text;
        if (safe.length() > maxLength){
            safe = safe.substring(0,maxLength);
        }
        texts.put(userId, safe);
    }

    public String getText(Long userId){
        if (userId == null) return null;
        return texts.get(userId);
    }

    public void clear(Long userId){
        if (userId != null){
            texts.remove(userId);
        }
    }
}
