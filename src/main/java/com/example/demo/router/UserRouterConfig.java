package com.example.demo.router;

import com.example.demo.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class UserRouterConfig {

    @Bean
    public RouterFunction< ServerResponse> userRoutesV1(UserHandler handler) {
        return RouterFunctions.route()
                .GET("api/v1/users", accept(APPLICATION_JSON), handler::getUsers)
                .GET("api/v1/users/{publicId}", accept(APPLICATION_JSON), handler::getUserByPublicId)
                .POST("api/v1/users", accept(APPLICATION_JSON), handler::createUser)
                .PUT("api/v1/users/{publicId}", accept(APPLICATION_JSON), handler::updateUser)
                .PATCH("api/v1/users/{publicId}", accept(APPLICATION_JSON), handler::patchUser)
                .DELETE("api/v1/users/{publicId}", accept(APPLICATION_JSON), handler::deleteUser)
                .build();
    }

}
