package com.agnostik.agnostik_app.core;

import com.agnostik.agnostik_app.authentication.JwtService;
import com.agnostik.agnostik_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class StompAuthChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Bean
    public ChannelInterceptor stompAuthInterceptor(){
        return new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String auth = accessor.getFirstNativeHeader("Authorization");
                    if (auth == null || !auth.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Missing of invalid authorization header");
                    }
                    String token = auth.substring(7);

                    String subject = jwtService.extractSubject(token);
                    Long userId = Long.parseLong(subject);

                    if (!jwtService.isTokenValid(token, userId)){
                        throw new IllegalArgumentException("Invalid jwt");
                    }

                    var authentication = new UsernamePasswordAuthenticationToken(
                            subject,
                            null,
                            List.of()
                    );
                    accessor.setUser(authentication);
                }
                return message;
            }
        };
    }
}
