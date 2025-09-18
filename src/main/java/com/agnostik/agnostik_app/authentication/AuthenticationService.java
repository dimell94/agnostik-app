package com.agnostik.agnostik_app.authentication;


import com.agnostik.agnostik_app.dto.AuthenticationRequestDTO;
import com.agnostik.agnostik_app.dto.AuthenticationResponseDTO;
import com.agnostik.agnostik_app.dto.UserRegisterDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponseDTO register(UserRegisterDTO dto){

        if (userRepository.existsByUsername(dto.getUsername())){
            throw new IllegalArgumentException("username already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId());

        return AuthenticationResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }

    public AuthenticationResponseDTO login (AuthenticationRequestDTO dto) {

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow( () -> new IllegalArgumentException("bad credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("bad credentials");
        }

        String token = jwtService.generateToken(user.getId());

        return AuthenticationResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }
}
