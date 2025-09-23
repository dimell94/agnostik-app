package com.agnostik.agnostik_app.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class EphemeralRequestService {

    private final PresenceService presenceService;

    @Data
    @AllArgsConstructor
    public static class Request{
        private long senderId;
        private long receiverId;
    }

    private final Map<String, Request> requests = new ConcurrentHashMap<>();

    private String key(long a, long b){
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return min + ":" + max;
    }

    public synchronized void send(long senderId, long receiverId){

        var neighbors = presenceService.getNeighbors(senderId);

        boolean adjacent =
                (neighbors.leftUserId != null && neighbors.leftUserId == receiverId)
                || (neighbors.rightUserId != null && neighbors.rightUserId == receiverId);

        if (!adjacent){
            throw new IllegalStateException("NOT_ADJACENT");
        }

        if (!presenceService.isLocked(senderId) || !presenceService.isLocked(receiverId)){
            throw new IllegalStateException("NOT_LOCKED");
        }

        String k = key(senderId, receiverId);

        requests.putIfAbsent(k,new Request(senderId,receiverId));

        log.info("Request: {} -> {}",senderId, receiverId);
    }


    public Optional<Request> get(long a, long b){
        String k = key(a, b);

        var neighbors = presenceService.getNeighbors(a);

        boolean stillAdjacent =
                (neighbors.leftUserId != null && neighbors.leftUserId == b)
                || (neighbors.rightUserId != null && neighbors.rightUserId == b);

        if (!stillAdjacent){
            requests.remove(k);
            return Optional.empty();
        }

        return Optional.ofNullable(requests.get(k));
    }

    public void cancel(long a, long b){
        requests.remove(key(a,b));
        log.info("Request canceled between {} and {}",a, b);
    }

    public boolean hasOutgoing(long userId, long neighborId){
        return get(userId, neighborId).map(r -> r.senderId == userId).orElse(false);
    }

    public boolean hasIncoming(long userId, long neighborId){
        return get(userId, neighborId).map(r -> r.receiverId == userId).orElse(false);
    }

    public void removeRequest(long a, long b){
        requests.remove(key(a, b));
    }


}
