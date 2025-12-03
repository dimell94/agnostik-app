package com.agnostik.agnostik_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NeighborsDTO {
    public final Long leftUserId;
    public final boolean leftLocked;
    public final Long rightUserId;
    public final boolean rightLocked;

}
