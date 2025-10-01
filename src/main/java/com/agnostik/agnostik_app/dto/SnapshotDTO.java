package com.agnostik.agnostik_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotDTO {

    private final String type = "SNAPSHOT";

    private Long meId;
    private String meUsername;

    private int myIndex;
    private int corridorSize;
    private boolean locked;

    private Long leftUserId;
    private Boolean leftLocked;
    private Boolean leftFriend;
    private String leftText;

    private Long rightUserId;
    private Boolean rightLocked;
    private Boolean rightFriend;
    private String rightText;

    private List<Long> requestsOutgoing;
    private List<Long> requestsIncoming;
}
