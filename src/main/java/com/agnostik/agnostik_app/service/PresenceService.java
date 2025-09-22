package com.agnostik.agnostik_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PresenceService {

    private final List<Long> corridor = new ArrayList<>();
    private final Object corridorLock = new Object();
    private final Map<Long, Boolean> locked = new ConcurrentHashMap<>();
    private final Set<String> protectedPairs = ConcurrentHashMap.newKeySet();

    private String pairKey(long a, long b){
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return min + ":" + max;
    }

    public void join(long userId){
        synchronized (corridorLock){
            if (!corridor.contains(userId)){
                corridor.add(userId);
                locked.putIfAbsent(userId, false);
                log.info("User {} joined (size={}", userId, corridor.size());

            }
        }
    }

    public void leave(long userId){
        synchronized (corridorLock){
            int idx = corridor.indexOf(userId);
            if (idx >= 0){
                Long left = (idx - 1) >= 0 ? corridor.get(idx - 1) : null;
                Long right = (idx + 1) <= corridor.size() ? corridor.get(idx + 1) : null;

                if (left != null) protectedPairs.remove(pairKey(left, userId));
                if (right != null) protectedPairs.remove(pairKey(userId, right));

                corridor.remove(idx);
                locked.remove(userId);

                log.info("User {} left (size={})", userId, corridor.size());
            }
        }
    }

    public void lock (long userId){
        locked.put(userId, true);

        synchronized (corridorLock){
            int idx = corridor.indexOf(userId);

            Long left = (idx - 1) >= 0 ? corridor.get(idx - 1) : null;
            Long right = (idx + 1) <= corridor.size() ? corridor.get(idx + 1) : null;

            if (left != null && Boolean.TRUE.equals(locked.get(left))){
                protectedPairs.add(pairKey(left, userId));
            }

            if (right != null && Boolean.TRUE.equals(locked.get(right))){
                protectedPairs.add(pairKey(userId, right));
            }
        }
        log.info("User {} locked", userId);
    }

    public void unlock (long userId){
        locked.put(userId, false);

        synchronized (corridorLock){
            int idx = corridor.indexOf(userId);

            if (idx >= 0){
                Long left = (idx - 1) >= 0 ? corridor.get(idx - 1) : null;
                Long right = (idx + 1) <= corridor.size() ? corridor.get(idx + 1) : null;

                if (left != null) protectedPairs.remove(pairKey(left, userId));
                if (right != null) protectedPairs.remove(pairKey(userId, right));
            }
        }

        log.info("User {} unlocked", userId);
    }

    public static class Neighbors {
        public final Long leftUserId;
        public final boolean leftLocked;
        public final Long rightUserId;
        public final boolean righLocked;

        public Neighbors(Long leftUserId, boolean leftLocked, Long rightUserId, boolean rightLocked){
            this.leftUserId = leftUserId;
            this.leftLocked = leftLocked;
            this.rightUserId = rightUserId;
            this.righLocked = rightLocked;
        }
    }

    public Neighbors getNeighbors(long userId){
        synchronized (corridorLock){
            int idx = corridor.indexOf(userId);
            if (idx < 0) {

                return new Neighbors(null, false, null, false);
            }

            Long left = (idx - 1) >= 0 ? corridor.get(idx - 1) : null;
            Long right = (idx + 1) < corridor.size() ? corridor.get(idx + 1) : null;

            boolean leftLocked = left != null && Boolean.TRUE.equals(locked.get(left));
            boolean rightLocked = right != null && Boolean.TRUE.equals(locked.get(right));

            return new Neighbors(left, leftLocked, right, rightLocked);
        }
    }

    public boolean canInterleave(long a, long b){
        return !protectedPairs.contains(pairKey(a, b));
    }

    public boolean isLocked (long userId){
        return Boolean.TRUE.equals(locked.get(userId));
    }
}
