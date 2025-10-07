package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.TextUpdateDTO;
import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import com.agnostik.agnostik_app.service.TextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/text/")
@RequiredArgsConstructor
public class TextRestController {

    private final TextService textService;
    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @PostMapping("/update")
    public ResponseEntity<?> updateText(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @RequestBody TextUpdateDTO body) {
        textService.updateText(me.getId(), body.getText());

        var neighbors = presenceService.getNeighbors(me.getId());

        Set<Long> impacted = new HashSet<>();
        impacted.add(me.getId());
        if (neighbors.getLeftUserId() != null) impacted.add(neighbors.getLeftUserId());
        if (neighbors.getRightUserId() != null) impacted.add(neighbors.getRightUserId());

        snapshotNotifierService.notifyUsers(impacted);

        return ResponseEntity.ok().body("User with id: " + me.getId() + "updated text");
    }
}
