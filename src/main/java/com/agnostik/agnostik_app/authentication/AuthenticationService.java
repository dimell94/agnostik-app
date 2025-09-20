package com.agnostik.agnostik_app.authentication;


import com.agnostik.agnostik_app.core.exception.AppInvalidCredentialsException;
import com.agnostik.agnostik_app.core.exception.AppObjectAlreadyExistsException;
import com.agnostik.agnostik_app.dto.AuthenticationRequestDTO;
import com.agnostik.agnostik_app.dto.AuthenticationResponseDTO;
import com.agnostik.agnostik_app.dto.UserRegisterDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponseDTO register(UserRegisterDTO dto){

        log.info("Register attempt for username='{}'", dto.getUsername());

        if (userRepository.existsByUsername(dto.getUsername())){
            log.warn("Register failed: username '{}' already exists", dto.getUsername());
            throw new AppObjectAlreadyExistsException("Username: '" + dto.getUsername() + "' already exists");
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

        log.info("Login attempt for username='{}'", dto.getUsername());

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow( () -> {
                    log.warn("Login failed: username '{}' not found", dto.getUsername());
                    return new AppInvalidCredentialsException();
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: wrong password for username='{}'", dto.getUsername());
            throw new AppInvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId());
        log.info("Login success: username='{}' (id={})", user.getUsername(), user.getId());

        return AuthenticationResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }
}
