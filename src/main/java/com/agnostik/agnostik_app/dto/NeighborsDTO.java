package com.agnostik.agnostik_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeighborsDTO {

    private Long leftUserId;
    private boolean leftLocked;
    private Long rightUserId;
    private boolean rightLocked;


}
