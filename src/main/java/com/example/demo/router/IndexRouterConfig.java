package com.example.demo.router;

import com.example.demo.handler.IndexHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class IndexRouterConfig {

    @Bean
    public RouterFunction< ServerResponse> indexRoutes(IndexHandler handler){
        return RouterFunctions.route()
                .GET("/", accept(APPLICATION_JSON), handler::index)
                .build();
    }

}
