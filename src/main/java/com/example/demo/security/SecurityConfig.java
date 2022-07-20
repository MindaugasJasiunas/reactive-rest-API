package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private final BearerTokenAuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http

                // register authentication manager & security context repo
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(this.authenticationManager)
                .securityContextRepository(this.securityContextRepository)
//                .securityContextRepository(this.securityContextRepository)
//                .addFilterAfter(new AuthenticationWebFilter(this.authenticationManager), SecurityWebFiltersOrder.REACTOR_CONTEXT)

                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/").permitAll()
                .pathMatchers("/api/v1/users/**").permitAll()
                .pathMatchers("/login", "/register").permitAll()
                .pathMatchers("/resettoken").permitAll()
                .anyExchange().authenticated()

                // disable CSRF
                .and().csrf().disable()
                // make application stateless
//                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())  // default 'WebSessionServerSecurityContextRepository' already overridden above with our custom implementation
                .requestCache().requestCache(NoOpServerRequestCache.getInstance())
                // disable POST /logout for stateless application
                .and()
                .logout().disable()
                // in case of 401 Unauthorized - return error (instead of trying to redirect to login page)
                .exceptionHandling()
                .authenticationEntryPoint((exchange, exception) -> Mono.error(exception))
                .accessDeniedHandler((exchange, exception) -> Mono.error(exception))

                .and().build();
    }



    /*
    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return (username) -> userRepository.findByUsername(username).flatMap(Mono::just);
    }
    */
}
