package com.agnostik.agnostik_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveResultDTO {

    private final int fromIndex;
    private final int toIndex;
    private final int corridorSize;
}
