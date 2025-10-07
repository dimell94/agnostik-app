package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/presence/")
@RequiredArgsConstructor
@Slf4j
public class PresenceRestController {

    private final PresenceService presenceService;

    @PostMapping("/leave")
    public ResponseEntity<?> leave(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.leave(me.getId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " left");

    }

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.lock(me.getId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " locked position");
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.unlock(me.getId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " unlocked position");
    }

    @PostMapping("/moveLeft")
    public ResponseEntity<?> moveLeft(@AuthenticationPrincipal UserReadOnlyDTO me){
         var moveResult = presenceService.moveLeft(me.getId());
        if (moveResult == null){
            return ResponseEntity.status(409).body("CANNOT_MOVE_LEFT");
        }

        return ResponseEntity.ok().body("User with id: " + me.getId() + " moved left");
    }

    @PostMapping("/moveRight")
    public ResponseEntity<?> moveRight(@AuthenticationPrincipal UserReadOnlyDTO me){
        var moved = presenceService.moveRight(me.getId());
        if (moved == null){
            return ResponseEntity.status(409).body("CANNOT_MOVE_RIGHT");
        }

        return ResponseEntity.ok().body("User with id: " + me.getId() + " moved right");
    }


}
