package com.agnostik.agnostik_app.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 2, max = 30, message = "Minimum characters: 2 , Maximum characters: 30")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Allowed only: letters, numbers, dots, hyphens, underscores ")
    private String username;

    @NotBlank
    @Size(min = 9, message = "Minimum characters: 9")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
