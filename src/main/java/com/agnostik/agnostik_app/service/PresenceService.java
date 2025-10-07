package com.agnostik.agnostik_app.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class PresenceService {

    private final List<Long> corridor = new LinkedList<>();
    private final Object corridorLock = new Object();
    private final Map<Long, Boolean> locked = new ConcurrentHashMap<>();
    private final Map<Long, Integer> indexByUser = new ConcurrentHashMap<>();
    private final Map<Pair<Long, Long>, Integer> unlockedFriendMoveCount = new ConcurrentHashMap<>();

    private final FriendshipService friendshipService;

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
                autoLockForFriends();

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

                unlockedFriendMoveCount.keySet().removeIf(p -> p.getFirst().equals(userId) || p.getSecond().equals(userId));
                autoLockForFriends();
            }
        }
    }

    public void lock (long userId){
        locked.put(userId, true);
        log.info("User {} locked", userId);
    }

    public void unlock (long userId){
        locked.put(userId, false);
        Neighbors neighbors = getNeighbors(userId);

        if (neighbors.getLeftUserId() != null && friendshipService.areFriends(userId, neighbors.getLeftUserId())) {
            unlockedFriendMoveCount.put(pairOf(userId, neighbors.getLeftUserId()), 0);
        }

        if (neighbors.getRightUserId() != null && friendshipService.areFriends(userId, neighbors.getRightUserId())) {
            unlockedFriendMoveCount.put(pairOf(userId, neighbors.getRightUserId()), 0);
        }

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
                incrementUnlockedFriendsCountForUser(userId);
                autoLockForFriends();
                return new MoveResult(userId, i, i + 1);
            }

            int j = i + 1;
            while (j < corridor.size() && locked.getOrDefault(corridor.get(j), false)){
                j++;
            }

            if (j == corridor.size()) return null;

            moveUser(i, j);
            incrementUnlockedFriendsCountForUser(userId);
            autoLockForFriends();
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
                incrementUnlockedFriendsCountForUser(userId);
                autoLockForFriends();
                return new MoveResult(userId, i, i - 1 );
            }

            int j = i - 1;
            while(j > 0 && locked.getOrDefault(corridor.get(j), false)){
                j--;
            }

            if(j < 0) return null;

            moveUser(i, j + 1);
            incrementUnlockedFriendsCountForUser(userId);
            autoLockForFriends();
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

            boolean leftLocked = left != null && isLocked(left);
            boolean rightLocked = right != null && isLocked(right);

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

    public Integer getIndexOf (Long userId){
        return indexByUser.get(userId);
    }

    public int getCorridorSize (){
        return corridor.size();
    }

    private void autoLockForFriends() {
        for (int i = 0; i < corridor.size() - 1; i++){
            Long leftId = corridor.get(i);
            Long rightId = corridor.get(i + 1);

            if (friendshipService.areFriends(leftId, rightId)){
                Pair<Long, Long> key = pairOf(leftId, rightId);
                Integer counter = unlockedFriendMoveCount.get(key);

                if (counter != null && counter < 2){
                    continue;
                }

                if (counter != null && counter >= 2){
                    unlockedFriendMoveCount.remove(key);
                }
                boolean leftLocked = locked.getOrDefault(leftId, false);
                boolean rightLocked = locked.getOrDefault(rightId, false);

                if (!leftLocked || !rightLocked){
                    locked.put(leftId, true);
                    locked.put(rightId, true);
                    log.info("Users: " + leftId + ", " + rightId + " were auto locked");
                }
            }
        }
    }

    private Pair<Long, Long> pairOf(Long a, Long b) {
        return a < b ? Pair.of(a, b) : Pair.of(b, a);
    }

    private void incrementUnlockedFriendsCountForUser (Long userId){
        var keys = unlockedFriendMoveCount.keySet().stream()
                .filter(p -> p.getFirst().equals(userId) || p.getSecond().equals(userId))
                .toList();

        for (var k : keys) {
            int newValue = unlockedFriendMoveCount.get(k) + 1;
            unlockedFriendMoveCount.put(k, newValue);
        }
    }



}
