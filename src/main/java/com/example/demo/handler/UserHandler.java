package com.example.demo.handler;

import com.example.demo.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserHandler {
    private final UserService service;

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(service.getUsers(), User.class);
    }

    public Mono<ServerResponse> getUserByPublicId(ServerRequest request) {
        String publicId = request.pathVariable("publicId");

        return service.getUserByPublicId(publicId)
                //happy path
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                // error path
                .switchIfEmpty(ServerResponse.notFound().build());
//                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage())));
    }


    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(User.class)
                .flatMap(service::createUser)
                .flatMap(createdUser -> ServerResponse.created(URI.create("http://localhost:8080/api/v1/users/" + createdUser.getPublicId())).build())
                .switchIfEmpty(ServerResponse.badRequest().build())
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage())));
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String publicId = request.pathVariable("publicId");
        return request.bodyToMono(User.class)
                .flatMap(userToSave -> service.updateUser(userToSave, publicId))
                .flatMap(savedUser -> ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.badRequest().build())
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage())));
    }

    public Mono<ServerResponse> patchUser(ServerRequest request) {
        String publicId = request.pathVariable("publicId");

        return request.bodyToMono(User.class)
                .flatMap(userToSave -> service.patchUser(userToSave, publicId))
                .flatMap(savedUser -> ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.badRequest().build())
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage())));
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String publicId = request.pathVariable("publicId");

        return service.deleteUser(publicId)
                .flatMap(unused -> ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.noContent().build());
    }
}
