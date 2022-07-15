package com.example.demo.handler;

import com.example.demo.AuthRequest;
import com.example.demo.User;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor

@Component
public class AuthHandler {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtUtil;

    public Mono<ServerResponse> login(ServerRequest request){
        Mono<AuthRequest> loginRequest = request.bodyToMono(AuthRequest.class);

        return loginRequest
                .flatMap(authRequest -> userDetailsService.findByUsername(authRequest.username())
                        .filter(userDetails -> passwordEncoder.matches(authRequest.password(), userDetails.getPassword()))
                )
                .flatMap(userDetails -> ServerResponse.ok().header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtUtil.generateJwtToken(userDetails))).build())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username/Password is invalid")))
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username/Password is invalid")));
    }


    public Mono<ServerResponse> register(ServerRequest request){
        Mono<User> userMono = request.bodyToMono(User.class);

        return userMono
                .flatMap(userService::registerUser)
                .flatMap(user -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage())));
    }

}
