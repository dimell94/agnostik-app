package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.service.EphemeralRequestService;
import com.agnostik.agnostik_app.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class EphemeralRequestsRestController {

    private final EphemeralRequestService ephemeralRequestService;
    private final FriendshipService friendshipService;

    @PostMapping("/send/{neighborId}")
    public ResponseEntity<?> sendRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable long neighborId){
        try{
            ephemeralRequestService.send(me.getId(), neighborId);
            return ResponseEntity.ok().body("Request sent to user " + neighborId);

        }catch(IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel/{neighborId}")
    public ResponseEntity<?> cancelRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable long neighborId){
        boolean hasOutgoing = ephemeralRequestService.hasOutgoing(me.getId(), neighborId);
        if (!hasOutgoing){
            return ResponseEntity.status(409).body("NO_OUTGOING_REQUEST");
        }

        ephemeralRequestService.cancel(me.getId(), neighborId);
        return ResponseEntity.ok().body("Request cancelled for user " + neighborId);
    }

    @GetMapping("/status/{neighborId}")
    public Map<String, Boolean> requestStatus(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable long neighborId){

        boolean outgoing = ephemeralRequestService.hasOutgoing(me.getId(), neighborId);
        boolean incoming = ephemeralRequestService.hasIncoming(me.getId(), neighborId);

        return Map.of("outgoing", outgoing, "incoming" , incoming);
    }

    @PostMapping("/accept/{neighborId}")
    public ResponseEntity<?> acceptRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable long neighborId){

        boolean hasIncoming = ephemeralRequestService.hasIncoming(me.getId(), neighborId);
        if(!hasIncoming) return ResponseEntity.status(409).body("NO_INCOMING_REQUEST");

        friendshipService.createFriendship(me.getId(), neighborId);
        ephemeralRequestService.removeRequest(me.getId(), neighborId);

        return ResponseEntity.ok().body("Friendship created with user " + neighborId);
    }




}
