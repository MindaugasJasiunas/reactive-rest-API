package com.example.demo.handler;

import com.example.demo.*;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RequiredArgsConstructor

@Component
public class AuthHandler {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtUtil;

    public Mono<ServerResponse> login(ServerRequest request){
        Mono<AuthRequest> loginRequest = request.bodyToMono(AuthRequest.class);

        return loginRequest
                .flatMap(authRequest -> userDetailsService.findByUsername(authRequest.username())
                        .filter(userDetails -> passwordEncoder.matches(authRequest.password(), userDetails.getPassword()))
                        .filter(userDetails -> userDetails.isEnabled() == true)
                        .filter(userDetails -> userDetails.isAccountNonExpired() == true)
                        .filter(userDetails -> userDetails.isAccountNonLocked() == true)
                        .filter(userDetails -> userDetails.isCredentialsNonExpired() == true)
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
                .switchIfEmpty(ServerResponse.badRequest().build())
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage())));
    }

    public Mono<ServerResponse> resetAccessToken(ServerRequest request){
        String refreshToken = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        if(refreshToken == null) return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired or invalid. Please login."));

        refreshToken = refreshToken.substring(7);

        if(jwtUtil.isTokenValid(refreshToken)){
            String username = jwtUtil.getSubject(refreshToken);

            // find user by username & if exists - generate new token & return
            String finalRefreshToken = refreshToken;
            return userDetailsService.findByUsername(username)
                    .flatMap(userDetails -> ServerResponse.ok().header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtUtil.refreshAccessToken(userDetails, finalRefreshToken))).build())
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired or invalid. Please login.")));
        }
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired or invalid. Please login."));
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        Mono<PasswordResetRequest> req = request.bodyToMono(PasswordResetRequest.class);

        return req.map(passwordResetRequest -> passwordResetRequest.getUsername())
                .flatMap(userService::findUserByUsername)
                .flatMap(user -> {
                    // PATCH to not change all user & re-encrypt password
                    user.setEnabled(false);
                    user.setPassword(null);
                    return userService.patchUser(user, user.getPublicId().toString());
                })
                .flatMap(user -> passwordResetService.createPasswordResetLink(user.getUsername()))
                .flatMap(passwordReset -> ServerResponse.ok().body(BodyInserters.fromValue(passwordReset.getLink())))
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    public Mono<ServerResponse> restorePassword(ServerRequest request) {
        Mono<PasswordRestoreRequest> req = request.bodyToMono(PasswordRestoreRequest.class);
        Optional<String> resetTokenOptional = request.queryParam("token");
        if(resetTokenOptional.isPresent()){
            String resetToken = resetTokenOptional.get();

            // find username by resetLink
            // find user by username
            // set new password(if passwords match) & isEnabled = true AND save user
            // delete resetToken from repo

            return passwordResetService.getUsernameByPasswordResetLink(resetToken)
                    .map(PasswordReset::getUsername)
                    .flatMap(userService::findUserByUsername)
                    .zipWith(req)
                    .flatMap(tuple -> {
                        if(!tuple.getT2().getPassword().equals(tuple.getT2().getRepeatedPassword())) return Mono.empty();
                        User u = new User();
                        u.setPublicId(tuple.getT1().getPublicId());
                        u.setPassword(tuple.getT2().getPassword());
                        u.setEnabled(true);
                        return userService.patchUser(u, u.getPublicId().toString());
                    })
                    .flatMap(user -> passwordResetService.deletePasswordResetByLink(resetToken))
                    .flatMap(unused -> ServerResponse.ok().build());
//                    .switchIfEmpty(ServerResponse.badRequest().build()); // when deleting password reset - always empty
        }
//        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Functionality not implemented yet."));
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide reset password token and try again: /restore?token=<tokenHere>"));
    }
}
