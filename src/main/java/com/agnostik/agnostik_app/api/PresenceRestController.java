package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.MoveResultDTO;
import com.agnostik.agnostik_app.dto.SnapshotDTO;
import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/presence/")
@RequiredArgsConstructor
@Slf4j
public class PresenceRestController {

    private final PresenceService presenceService;
    private final SnapshotService snapshotService;

    @PostMapping("/leave")
    public ResponseEntity<?> leave(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.leave(me.getId());
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.lock(me.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.unlock(me.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/moveLeft")
    public ResponseEntity<?> moveLeft(@AuthenticationPrincipal UserReadOnlyDTO me) {
        MoveResultDTO result = presenceService.moveLeft(me.getId());
        if (result == null) {
            return ResponseEntity.status(409).body(Map.of("ok", false));
        }
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "result", Map.of(
                        "userId", result.getUserId(),
                        "fromIndex", result.getFromIndex(),
                        "toIndex", result.getToIndex(),
                        "corridorSize", presenceService.getCorridorSize()
                )
        ));
    }

    @PostMapping("/moveRight")
    public ResponseEntity<?> moveRight(@AuthenticationPrincipal UserReadOnlyDTO me) {
        MoveResultDTO result = presenceService.moveRight(me.getId());
        if (result == null) {
            return ResponseEntity.status(409).body(Map.of("ok", false));
        }
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "result", Map.of(
                        "userId", result.getUserId(),
                        "fromIndex", result.getFromIndex(),
                        "toIndex", result.getToIndex(),
                        "corridorSize", presenceService.getCorridorSize()
                )
        ));
    }

    @GetMapping("/neighbors")
    public ResponseEntity<?> neighbors(@AuthenticationPrincipal UserReadOnlyDTO me){
        var neighbors = presenceService.getNeighbors(me.getId());
        return ResponseEntity.ok(neighbors);
    }

    @GetMapping("/snapshot")
    public ResponseEntity<SnapshotDTO> snapshot(@AuthenticationPrincipal UserReadOnlyDTO me){
        SnapshotDTO snapshotDTO = snapshotService.getSnapshot(me.getId());
        return ResponseEntity.ok(snapshotDTO);
    }

    @PostMapping("/text")
    public ResponseEntity<?> updateText(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @RequestBody Map<String, String> body
            ){
        String text = body.getOrDefault("text", "");
        presenceService.updateText(me.getId(), text);
        return ResponseEntity.noContent().build();
    }
}
