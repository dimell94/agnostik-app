package com.agnostik.agnostik_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveResultDTO {
    private long userId;
    private int fromIndex;
    private int toIndex;
}
