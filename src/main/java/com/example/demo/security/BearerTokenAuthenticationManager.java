package com.example.demo.security;

import com.example.demo.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor

@Component
@Primary
public class BearerTokenAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtTokenProvider jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        return Mono.just(jwtUtil.isTokenValid(authToken))
                .filter(valid -> valid)
                .switchIfEmpty(Mono.empty())
                .map(valid -> {
                    Set<SimpleGrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(authToken);
                    return new UsernamePasswordAuthenticationToken(
                            jwtUtil.getSubject(authToken),
                            null,
                            authorities
                    );
                });
    }
}
