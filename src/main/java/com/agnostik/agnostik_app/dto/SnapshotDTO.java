package com.agnostik.agnostik_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotDTO {

    private UserView me;
    private NeighborView left;
    private NeighborView right;
    private CorridorInfo corridor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserView {
        private Long id;
        private String text;
        private boolean locked;
        private Integer myIndex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NeighborView {
        private Long id;
        private String text;
        private boolean locked;
        private boolean friend;
        private boolean requestToMe;
        private boolean requestFromMe;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorridorInfo {
        private Integer size;
    }
}
