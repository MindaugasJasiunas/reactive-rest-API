package com.example.demo.router;

import com.example.demo.handler.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AuthRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler authHandler){
        return RouterFunctions.route()
                .POST("/login", RequestPredicates.accept(MediaType.APPLICATION_JSON), authHandler::login)
                .POST("/register", RequestPredicates.accept(MediaType.APPLICATION_JSON), authHandler::register)
                .POST("/resettoken", RequestPredicates.accept(MediaType.APPLICATION_JSON), authHandler::resetAccessToken)
                .build();
    }
}
