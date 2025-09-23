package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/friendships")
@RequiredArgsConstructor
@Slf4j
public class FriendshipRestController {

    private final FriendshipService friendshipService;

    @GetMapping("/status/{otherUserId}")
    public Map<String, Boolean> status(@PathVariable Long otherUserId, @AuthenticationPrincipal UserReadOnlyDTO me) {
        boolean friends = friendshipService.areFriends(me.getId(), otherUserId);

        return Map.of("friends", friends);
    }

//    public ResponseEntity<Map<String, Object>> create(@PathVariable Long otherUserId, @AuthenticationPrincipal User me){
//        friendshipService.createFriendship(me.getId(), otherUserId);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(Map.of(
//                        "success", true,
//                        "message", "Friendship created",
//                        "user1Id", me.getId(),
//                        "user2Id", otherUserId
//                ));
//    }




}
