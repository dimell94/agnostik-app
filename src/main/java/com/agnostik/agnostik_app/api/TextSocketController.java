package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.TextUpdateDTO;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import com.agnostik.agnostik_app.service.TextService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class TextSocketController {

    private final TextService textService;
    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @MessageMapping("/text")
    public void updateText(Principal principal, TextUpdateDTO body){

        Long userId = Long.parseLong(principal.getName());

        textService.updateText(userId, body.getText());

        var neighbors = presenceService.getNeighbors(userId);

        Set<Long> impacted = new HashSet<>();
        impacted.add(userId);
        if (neighbors.getLeftUserId() != null) impacted.add(neighbors.getLeftUserId());
        if (neighbors.getRightUserId() != null) impacted.add(neighbors.getRightUserId());

        snapshotNotifierService.notifyUsers(impacted);
    }

}
