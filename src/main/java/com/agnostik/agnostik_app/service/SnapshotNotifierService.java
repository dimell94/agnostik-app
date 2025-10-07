package com.agnostik.agnostik_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SnapshotNotifierService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SnapshotService snapshotService;

    public void notifyUsers(Set<Long> userIds){
        for (Long id : userIds){
            var snapshot = snapshotService.buildFor(id);
            messagingTemplate.convertAndSendToUser(
                    id.toString(),
                    "/queue/snapshot",
                    snapshot
            );
        }
    }
}
