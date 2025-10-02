package com.agnostik.agnostik_app.service;


import com.agnostik.agnostik_app.dto.MoveResultDTO;
import com.agnostik.agnostik_app.dto.NeighborsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j

public class PresenceService {

    private final List<Long> corridor = new LinkedList<>();
    private final Object corridorLock = new Object();
    private final Map<Long, Boolean> locked = new ConcurrentHashMap<>();
    private final Map<Long, Integer> indexByUser = new ConcurrentHashMap<>();

    @Autowired
    NeighborTextStoreService neighborTextStoreService;

//    @Data
//    @AllArgsConstructor
//    public static class MoveResult {
//        private final long userId;
//        private final int fromIndex;
//        private final int toIndex;
//    }


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

    public MoveResultDTO moveRight(long userId){
        NeighborsDTO before;
        MoveResultDTO result;
        synchronized (corridorLock){
            before = getNeighbors(userId);
            result = doMoveRight(userId);
        }
        //to add notifier
        return result;
    }

    public MoveResultDTO moveLeft(long userId){
        NeighborsDTO before;
        MoveResultDTO result;
        synchronized (corridorLock){
            before = getNeighbors(userId);
            result = doMoveLeft(userId);
        }
        //to add notifier
        return result;
    }

    public void lock (long userId){
        locked.put(userId, true);
        log.info("User {} locked", userId);
    }

    public void unlock (long userId){
        locked.put(userId, false);
        log.info("User {} unlocked", userId);
    }







//    @Data
//    @AllArgsConstructor
//    public static class Neighbors {
//        public final Long leftUserId;
//        public final boolean leftLocked;
//        public final Long rightUserId;
//        public final boolean rightLocked;
//
//    }

    public NeighborsDTO getNeighbors(long userId){
        synchronized (corridorLock){
            int idx = corridor.indexOf(userId);
            if (idx < 0) {

                return new NeighborsDTO(null, false, null, false);
            }

            Long left = (idx - 1) >= 0 ? corridor.get(idx - 1) : null;
            Long right = (idx + 1) < corridor.size() ? corridor.get(idx + 1) : null;

            boolean leftLocked = left != null && Boolean.TRUE.equals(locked.get(left));
            boolean rightLocked = right != null && Boolean.TRUE.equals(locked.get(right));

            return new NeighborsDTO(left, leftLocked, right, rightLocked);
        }
    }



    public boolean isLocked (long userId){
        return Boolean.TRUE.equals(locked.get(userId));
    }

    private MoveResultDTO doMoveRight(long userId){

            Integer iObj = indexByUser.get(userId);
            if (iObj == null) return null;
            int i = iObj;

            if (i == corridor.size() - 1) return null;

            Long right = corridor.get(i + 1);
            if(!locked.getOrDefault(right, false)){
                swapPositions(i, i + 1);
                return new MoveResultDTO(userId, i, i + 1);
            }

            int j = i + 1;
            while (j < corridor.size() && locked.getOrDefault(corridor.get(j), false)){
                j++;
            }

            if (j == corridor.size()) return null;

            moveUser(i, j);
            return new MoveResultDTO(userId, i, j);

    }

    private MoveResultDTO doMoveLeft(long userId){

            Integer iObj = indexByUser.get(userId);
            if (iObj == null) return null;
            int i = iObj;

            if (i == 0) return null;

            Long left = corridor.get(i - 1);
            if(!locked.getOrDefault(left, false)){
                swapPositions(i, i -1);
                return new MoveResultDTO(userId, i, i - 1 );
            }

            int j = i - 1;
            while(j > 0 && locked.getOrDefault(corridor.get(j), false)){
                j--;
            }

            if(j < 0) return null;

            moveUser(i, j + 1);
            return  new MoveResultDTO(userId, i, j + 1);



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

    public int getMyIndex(Long userId){
        synchronized (corridorLock){
            return corridor.indexOf(userId);
        }

    }

    public int getCorridorSize(){
        synchronized (corridorLock) {
            return corridor.size();
        }
    }

    public void updateText(Long userId, String text) {
        neighborTextStoreService.setText(userId, text == null ? "" : text);

    }



}
