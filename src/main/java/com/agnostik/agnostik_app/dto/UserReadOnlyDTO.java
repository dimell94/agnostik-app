package com.agnostik.agnostik_app.dto;

import com.agnostik.agnostik_app.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserReadOnlyDTO {
    private Long id;
    private String username;

    public static UserReadOnlyDTO from(User user){
        return new UserReadOnlyDTO(user.getId(), user.getUsername());
    }
}
