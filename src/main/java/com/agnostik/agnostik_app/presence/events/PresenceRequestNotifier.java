package com.agnostik.agnostik_app.presence.events;

import com.agnostik.agnostik_app.presence.messaging.PresenceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceRequestNotifier {

    private final PresenceEventPublisher eventPublisher;

    public void notifyRequestSent(long fromUserId, long toUserId) {
        Map<String, Object> payload = Map.of(
                "type", "REQUEST_INCOMING",
                "data", Map.of("fromUserId", fromUserId)
        );
        eventPublisher.sendToUser(toUserId, "/queue/presence", payload);
    }

    public void notifyRequestCancelled(long fromUserId, long toUserId) {
        Map<String, Object> payload = Map.of(
                "type", "REQUEST_CANCELLED",
                "data", Map.of("fromUserId", fromUserId)
        );
        eventPublisher.sendToUser(toUserId, "/queue/presence", payload);
        eventPublisher.sendToUser(fromUserId, "/queue/presence", payload);
    }

    public void notifyRequestAccepted(long fromUserId, long toUserId) {
        Map<String, Object> payload = Map.of(
                "type", "REQUEST_ACCEPTED",
                "data", Map.of("byUserId", toUserId)
        );
        eventPublisher.sendToUser(fromUserId, "/queue/presence", payload);
        eventPublisher.sendToUser(toUserId, "/queue/presence", payload);
    }

    public void notifyRequestRejected(long fromUserId, long toUserId) {
        Map<String, Object> payload = Map.of(
                "type", "REQUEST_REJECTED",
                "data", Map.of("byUserId", toUserId)
        );
        eventPublisher.sendToUser(fromUserId, "/queue/presence", payload);
        eventPublisher.sendToUser(toUserId, "/queue/presence", payload);
    }

    public void notifyFriendshipCreated(long userId1, long userId2) {
        Map<String, Object> payload = Map.of(
                "type", "FRIENDSHIP_CREATED",
                "data", Map.of("userId1", userId1, "userId2", userId2)
        );
        eventPublisher.sendToUser(userId1, "/queue/presence", payload);
        eventPublisher.sendToUser(userId2, "/queue/presence", payload);
    }
}
