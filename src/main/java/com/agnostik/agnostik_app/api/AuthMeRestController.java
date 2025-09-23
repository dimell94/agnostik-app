package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthMeRestController {

    @GetMapping("/me")
    public UserReadOnlyDTO me(@AuthenticationPrincipal UserReadOnlyDTO me){

        return me;
    }
}
