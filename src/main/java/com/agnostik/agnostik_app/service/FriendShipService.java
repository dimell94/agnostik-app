package com.agnostik.agnostik_app.service;

import com.agnostik.agnostik_app.core.exception.AppGenericException;
import com.agnostik.agnostik_app.core.exception.AppObjectAlreadyExistsException;
import com.agnostik.agnostik_app.core.exception.AppObjectInvalidArgumentException;
import com.agnostik.agnostik_app.model.Friendship;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.repository.FriendshipRepository;
import com.agnostik.agnostik_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendShipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean areFriends(Long a, Long b){
        if (a == null || b == null) throw new AppObjectInvalidArgumentException("NULL_USER", "User cannot be null");
        if (a.equals(b)) return false;
        return friendshipRepository.areFriends(a,b);
    }

    @Transactional
    public Friendship createFriendship(Long a, Long b){
        if(a == null || b == null) throw new AppObjectInvalidArgumentException("NULL_USER_ID", "User Id cannot be null");
        if(a.equals(b)) throw new AppObjectInvalidArgumentException("SAME_USER", "User cannot be friend with themselves");

        Long smallerId = Math.min(a, b);
        Long largerId = Math.max(a, b);

        if (friendshipRepository.areFriends(smallerId,largerId)){
            throw new AppObjectAlreadyExistsException("Friendship already exists");
        }

        User user1 = userRepository.findById(smallerId)
                .orElseThrow(() -> new IllegalArgumentException("USER1_NOT_FOUND"));

        User user2 = userRepository.findById(largerId)
                .orElseThrow(() -> new IllegalArgumentException("USER2_NOT_FOUND"));

        Friendship friendship = new Friendship();
        friendship.setUser1(user1);
        friendship.setUser2(user2);

        Friendship saved = friendshipRepository.save(friendship);
        log.info("Friendship created between users with Ids: {}, {}", smallerId, largerId);


        return saved;


    }
}
