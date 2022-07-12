package com.example.demo.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@Component
public class IndexHandler {

    public Mono< ServerResponse> index(ServerRequest request) {
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(BodyInserters.fromValue("Users reactive REST API. URL: /api/v1/users/"));
    }

}
