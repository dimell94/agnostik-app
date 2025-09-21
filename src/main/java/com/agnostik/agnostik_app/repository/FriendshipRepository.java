package com.agnostik.agnostik_app.repository;

import com.agnostik.agnostik_app.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByUser1_IdAndUser2_Id(Long u1, Long u2);

    @Query("""
            select (count(f) > 0) from Friendship f
            where (f.user1.id = :a and f.user2.id = :b)
            or (f.user1.id = :b and f.user2.id = :a)
            """)
    boolean areFriends(Long a, Long b);
}
