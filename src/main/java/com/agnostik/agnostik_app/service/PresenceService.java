package com.agnostik.agnostik_app.service;

import lombok.AllArgsConstructor;
import lombok.Data;
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
    private final Map<Long, Integer> indexByUser = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    public static class MoveResult {
        private final long userId;
        private final int fromIndex;
        private final int toIndex;
    }


    public void join(long userId){
        synchronized (corridorLock){
            if (!corridor.contains(userId)){
                corridor.add(userId);
                indexByUser.put(userId, corridor.size() - 1);
                locked.putIfAbsent(userId, false);
                log.info("User {} joined (size={}", userId, corridor.size());

            }
        }
    }

    public void leave(long userId){
        synchronized (corridorLock){
            int idx = corridor.indexOf(userId);
            if (idx >= 0){
                corridor.remove(idx);
                indexByUser.remove(userId);

                for(int i = idx; i < corridor.size(); i++){
                    indexByUser.put(corridor.get(i), i);
                }

                locked.remove(userId);

                log.info("User {} left (size={})", userId, corridor.size());
            }
        }
    }

    public void lock (long userId){
        locked.put(userId, true);
        log.info("User {} locked", userId);
    }

    public void unlock (long userId){
        locked.put(userId, false);
        log.info("User {} unlocked", userId);
    }

    public MoveResult moveRight(long userId){
        synchronized (corridorLock){
            Integer iObj = indexByUser.get(userId);
            if (iObj == null) return null;
            int i = iObj;

            if (i == corridor.size() - 1) return null;

            Long right = corridor.get(i + 1);
            if(!locked.getOrDefault(right, false)){
                swapPositions(i, i + 1);
                return new MoveResult(userId, i, i + 1);
            }

            int j = i + 1;
            while (j < corridor.size() && locked.getOrDefault(corridor.get(j), false)){
                j++;
            }

            if (j == corridor.size()) return null;

            moveUser(i, j);
            return new MoveResult(userId, i, j);
        }
    }

    public MoveResult moveLeft(long userId){
        synchronized (corridorLock){
            Integer iObj = indexByUser.get(userId);
            if (iObj == null) return null;
            int i = iObj;

            if (i == 0) return null;

            Long left = corridor.get(i - 1);
            if(!locked.getOrDefault(left, false)){
                swapPositions(i, i -1);
                return new MoveResult(userId, i, i - 1 );
            }

            int j = i - 1;
            while(j > 0 && locked.getOrDefault(corridor.get(j), false)){
                j--;
            }

            if(j < 0) return null;

            moveUser(i, j + 1);
            return  new MoveResult(userId, i, j + 1);


        }
    }



    @Data
    @AllArgsConstructor
    public static class Neighbors {
        public final Long leftUserId;
        public final boolean leftLocked;
        public final Long rightUserId;
        public final boolean rightLocked;

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



    public boolean isLocked (long userId){
        return Boolean.TRUE.equals(locked.get(userId));
    }

    private void swapPositions(int i, int j){
        Long u1 = corridor.get(i);
        Long u2 = corridor.get(j);
        corridor.set(i, u2);
        corridor.set(j, u1);
        indexByUser.put(u1, j);
        indexByUser.put(u2, i);
    }

    private void moveUser(int from, int to){
        Long user = corridor.remove(from);
        corridor.add(to, user);

        for (int i = Math.min(from, to); i < corridor.size(); i++){
            indexByUser.put(corridor.get(i), i);
        }
    }


}
