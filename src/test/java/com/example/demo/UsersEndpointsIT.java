package com.example.demo;

import com.example.demo.exception.CustomErrorAttributes;
import com.example.demo.exception.GlobalErrorWebExceptionHandler;
import com.example.demo.handler.UserHandler;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.router.UserRouterConfig;
import com.example.demo.security.BearerTokenAuthenticationManager;
import com.example.demo.security.SecurityConfig;
import com.example.demo.security.SecurityContextRepository;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.service.UserServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)  // setup test for web environment specifically (fully initialize spring boot)
@WebFluxTest
@Import({CustomErrorAttributes.class, GlobalErrorWebExceptionHandler.class, UserRouterConfig.class, UserHandler.class, UserServiceImpl.class,  SecurityConfig.class, BearerTokenAuthenticationManager.class, SecurityContextRepository.class})
public class UsersEndpointsIT {
    @MockBean
    UserRepository userRepository;
    @MockBean
    RoleRepository roleRepository;
    @MockBean
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    WebTestClient client;

    List<User> users;
    String userRoleToken;
    String managerRoleToken;
    String adminRoleToken;
    final String encodedPassword = "encodedPassword";

    @BeforeEach
    void setUp() {
//        client = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();

        users = new ArrayList<>();
//        users.add(new User(1L, UUID.fromString("d51f1234-3d7d-4100-8846-468f38e14a4f"), "johnd", "$2a$12$L61SNM2qG1YPyD4.bG02OOUBO.oW8QOT51CwMlgQ/7HibB8bhXXuO", "John", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 1L, new Role(1L, "ROLE_USER", List.of(new Authority(2L, "user:read"))), true, true, true, true));
//        users.add(new User(2L, UUID.fromString("07db1b55-714b-432e-af3b-5ad587a359e0"), "janed", "$2a$12$JVpKQOwi6gxeVlp6oUWlQuObzeyM8SFfWJtCxTOIJgn/TT4PCTle6", "Jane", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 2L, new Role(1L, "ROLE_MANAGER", List.of(new Authority(2L, "user:read"), new Authority(3L, "user:update"))), true, true, true, true));
//        users.add(new User(3L, UUID.fromString("d4c1ca44-e996-4ed9-80c9-d5d0a1f4b2ff"), "admin", "$2a$12$TpOiIo8Th4AwND6vxCgs.e.QNOka.m4hux9hVH4iz1DxIiUBrHzXe", "Tom", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 3L, new Role(1L, "ROLE_ADMIN", List.of(new Authority(1L, "user:create"), new Authority(2L, "user:read"), new Authority(3L, "user:update"), new Authority(4L, "user:delete"))), true, true, true, true));

        // users without roles - user service will populate users with roles by roleId field
        users.add(new User(1L, UUID.fromString("d51f1234-3d7d-4100-8846-468f38e14a4f"), "johnd", "$2a$12$L61SNM2qG1YPyD4.bG02OOUBO.oW8QOT51CwMlgQ/7HibB8bhXXuO", "John", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 1L, null, true, true, true, true));
        users.add(new User(2L, UUID.fromString("07db1b55-714b-432e-af3b-5ad587a359e0"), "janed", "$2a$12$JVpKQOwi6gxeVlp6oUWlQuObzeyM8SFfWJtCxTOIJgn/TT4PCTle6", "Jane", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 2L, null, true, true, true, true));
        users.add(new User(3L, UUID.fromString("d4c1ca44-e996-4ed9-80c9-d5d0a1f4b2ff"), "admin", "$2a$12$TpOiIo8Th4AwND6vxCgs.e.QNOka.m4hux9hVH4iz1DxIiUBrHzXe", "Tom", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 3L, null, true, true, true, true));

        Mockito.when(roleRepository.findById(1L)).thenReturn(Mono.just(new Role(1L, "ROLE_USER", null)));
        Mockito.when(roleRepository.findById(2L)).thenReturn(Mono.just(new Role(2L, "ROLE_MANAGER", null)));
        Mockito.when(roleRepository.findById(3L)).thenReturn(Mono.just(new Role(3L, "ROLE_ADMIN", null)));
        Mockito.when(roleRepository.getAuthoritiesByRoleId(1L)).thenReturn(Flux.just(new Authority(2L, "user:read")));
        Mockito.when(roleRepository.getAuthoritiesByRoleId(2L)).thenReturn(Flux.just(new Authority(2L, "user:read"), new Authority(3L, "user:update")));
        Mockito.when(roleRepository.getAuthoritiesByRoleId(3L)).thenReturn(Flux.just(new Authority(1L, "user:create"), new Authority(2L, "user:read"), new Authority(3L, "user:update"), new Authority(4L, "user:delete")));

        Mockito.when(passwordEncoder.encode(ArgumentMatchers.anyString())).thenReturn(encodedPassword);

        userRoleToken = jwtTokenProvider.generateJwtToken(new User(1L, UUID.fromString("d51f1234-3d7d-4100-8846-468f38e14a4f"), "johnd", "$2a$12$L61SNM2qG1YPyD4.bG02OOUBO.oW8QOT51CwMlgQ/7HibB8bhXXuO", "John", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 1L, new Role(1L, "ROLE_USER", List.of(new Authority(2L, "user:read"))), true, true, true, true));
        managerRoleToken = jwtTokenProvider.generateJwtToken(new User(2L, UUID.fromString("07db1b55-714b-432e-af3b-5ad587a359e0"), "janed", "$2a$12$JVpKQOwi6gxeVlp6oUWlQuObzeyM8SFfWJtCxTOIJgn/TT4PCTle6", "Jane", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 2L, new Role(1L, "ROLE_MANAGER", List.of(new Authority(2L, "user:read"), new Authority(3L, "user:update"))), true, true, true, true));
        adminRoleToken = jwtTokenProvider.generateJwtToken(new User(3L, UUID.fromString("d4c1ca44-e996-4ed9-80c9-d5d0a1f4b2ff"), "admin", "$2a$12$TpOiIo8Th4AwND6vxCgs.e.QNOka.m4hux9hVH4iz1DxIiUBrHzXe", "Tom", "Doe", LocalDateTime.of(2022,7,13, 12, 0), LocalDateTime.of(2022,7,13, 12, 0), 3L, new Role(1L, "ROLE_ADMIN", List.of(new Authority(1L, "user:create"), new Authority(2L, "user:read"), new Authority(3L, "user:update"), new Authority(4L, "user:delete"))), true, true, true, true));
    }

    @Test
    void getAllUsers_notAuth(){
        client.get().uri("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();

        Mockito.verify(userRepository, Mockito.never()).findAll();
    }

    @DisplayName("getAllUsers() with auth - user:read authority")
    @Test
    void getAllUsers_userReadAuth() {
        Mockito.when(userRepository.findAll()).thenReturn(Flux.fromIterable(users));

        client.get().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(userRoleToken)) // JWT
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class).value(userList -> {
                    Assertions.assertEquals(3, userList.size());
                    Assertions.assertEquals(users.get(0).getUsername(), userList.get(0).getUsername());
                    // assert that populateUserWithRolesAndAuthorities method is working as expected
                    Assertions.assertNotNull(userList.get(0).getRole());
                    Assertions.assertNotNull(userList.get(0).getRole().getAuthorities());
        });

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Mockito.verify(roleRepository, Mockito.times(3)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).getAuthoritiesByRoleId(ArgumentMatchers.anyLong());
    }

    @Test
    void getByPublicId_notAuth(){
        client.get().uri("/api/v1/users/"+users.get(0).getPublicId().toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();

        Mockito.verify(userRepository, Mockito.never()).findByPublicId(users.get(0).getPublicId().toString());
    }

    @DisplayName("getUserByPublicId(<publicId>) with auth - user:read authority")
    @Test
    void getUserByPublicId() {
        Mockito.when(userRepository.findByPublicId(users.get(0).getPublicId().toString())).thenReturn(Mono.just(users.get(0)));

        client.get().uri("/api/v1/users/"+users.get(0).getPublicId().toString())
                .headers(http -> http.setBearerAuth(userRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    Assertions.assertEquals(users.get(0).getUsername(), user.getUsername());
                    // assert that populateUserWithRolesAndAuthorities method is working as expected
                    Assertions.assertNotNull(user.getRole());
                    Assertions.assertEquals("ROLE_USER", user.getRole().getRoleName());
                    Assertions.assertNotNull(user.getRole().getAuthorities());
                    Assertions.assertEquals("user:read", user.getRole().getAuthorities().get(0).getAuthorityName());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).getAuthoritiesByRoleId(ArgumentMatchers.anyLong());
    }

    @DisplayName("createUser(<publicId>) with auth - user:create authority")
    @Test
    void createUser_defaultRole() throws JsonProcessingException {
        String username = "usrName";
        String password = "password";
        String firstName = "First";
        String lastName = "Last";
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User userToCreate = new User(null, uuid, username, password, firstName, lastName, null, null, null, null, true, true, true, true);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.empty());
        final ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userRepository.save(userArgumentCaptor.capture())).thenReturn(Mono.just(userToCreate));

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s" 
        }
        """, username, password, firstName, lastName);

        client.post().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(adminRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectHeader().exists("Location")
                .expectHeader().value("Location", Matchers.equalTo("http://localhost:8080/api/v1/users/"+uuid))
                .expectStatus().isCreated();

        Assertions.assertNotNull(userArgumentCaptor.getValue());
        Assertions.assertNotNull(userArgumentCaptor.getValue().getPublicId());
        Assertions.assertEquals(username, userArgumentCaptor.getValue().getUsername());
        Assertions.assertEquals(encodedPassword, userArgumentCaptor.getValue().getPassword());
        Assertions.assertEquals(firstName, userArgumentCaptor.getValue().getFirstName());
        Assertions.assertEquals(lastName, userArgumentCaptor.getValue().getLastName());
        Assertions.assertEquals(1L, userArgumentCaptor.getValue().getRoleId());
        Assertions.assertTrue(userArgumentCaptor.getValue().isAccountNonExpired());
        Assertions.assertTrue(userArgumentCaptor.getValue().isAccountNonLocked());
        Assertions.assertTrue(userArgumentCaptor.getValue().isCredentialsNonExpired());
        Assertions.assertTrue(userArgumentCaptor.getValue().isEnabled());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_customRole(){
        String username = "usrName";
        Long roleId = 3L;

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.empty());
        final ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userRepository.save(userArgumentCaptor.capture())).thenReturn(Mono.just(new User()));

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s",
            "roleId": %d
        }
        """, username, "password", "First", "Last", roleId);

        client.post().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(adminRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectHeader().exists("Location")
                .expectStatus().isCreated();

        Assertions.assertNotNull(userArgumentCaptor.getValue());
        Assertions.assertNotNull(userArgumentCaptor.getValue().getRoleId());
        Assertions.assertEquals(roleId, userArgumentCaptor.getValue().getRoleId());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_roleDoesntExist(){
        String username = "usrName";
        Long roleId = 4L;

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.empty());
        Mockito.when(roleRepository.findById(roleId)).thenReturn(Mono.empty());

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s",
            "roleId": %d
        }
        """, username, "password", "First", "Last", roleId);

        client.post().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(adminRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(string -> {
                    Assertions.assertTrue(string.contains("Role provided does not exist"));
                });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_userAlreadyExists(){
        String username = "userName";

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.just(new User()));

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s"
        }
        """, username, "password", "First", "Last");

        client.post().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(adminRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(string -> {
            Assertions.assertTrue(string.contains("User with provided username already exists"));
        });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_invalidPasswordField(){
        String invalidPassword = "pass";

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s"
        }
        """, "username", invalidPassword, "First", "Last");

        client.post().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(adminRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(string -> {
            Assertions.assertTrue(string.contains("Submitted user is not valid: Password not valid"));
        });

        Mockito.verify(userRepository, Mockito.never()).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.never()).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_notAuth(){
        client.post().uri("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isUnauthorized();

        Mockito.verify(userRepository, Mockito.never()).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.never()).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_invalidAuthorities(){
        client.post().uri("/api/v1/users")
                .headers(http -> http.setBearerAuth(userRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isForbidden();

        Mockito.verify(userRepository, Mockito.never()).findByUsername(ArgumentMatchers.anyString());
        Mockito.verify(roleRepository, Mockito.never()).findById(ArgumentMatchers.anyLong());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser(){
        UUID uuid = UUID.randomUUID();
        Long id = 5L;
        String username = "username";
        Long roleId = 2L;
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRoleId(roleId);
        user.setPublicId(uuid);

        String changedUsername = "customUsername";
        Long changedRoleId = 3L;

        Mockito.when(userRepository.findByPublicId(uuid.toString())).thenReturn(Mono.just(user));
        final ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userRepository.save(userArgumentCaptor.capture())).thenReturn(Mono.just(user));

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s",
            "roleId": "%s"
        }
        """, changedUsername, "password", "First", "Last", changedRoleId);

        client.put().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(managerRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isNoContent();

        Assertions.assertNotNull(userArgumentCaptor.getValue());
        Assertions.assertEquals(user.getId(), userArgumentCaptor.getValue().getId(), "ID should not be changed");
        Assertions.assertEquals(user.getPublicId(), uuid, "publicId should not be changed");
        Assertions.assertEquals(username, userArgumentCaptor.getValue().getUsername(), "Username should not be changed"); // not changed
        Assertions.assertEquals(encodedPassword, userArgumentCaptor.getValue().getPassword(), "Password should be encoded");
        Assertions.assertEquals(changedRoleId, userArgumentCaptor.getValue().getRoleId(), "Role ID must be changed to new one");

        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findById(changedRoleId);
        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(uuid.toString());
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_notAuth(){
        UUID uuid = UUID.randomUUID();
        client.put().uri("/api/v1/users/"+uuid)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isUnauthorized();

        Mockito.verify(roleRepository, Mockito.never()).findById(ArgumentMatchers.anyLong());
        Mockito.verify(userRepository, Mockito.never()).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_invalidField(){
        UUID uuid = UUID.randomUUID();
        String invalidPassword = "pass";

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s",
            "roleId": "%s"
        }
        """, "username", invalidPassword, "First", "Last", "3");

        client.put().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(managerRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(s -> Assertions.assertTrue(s.contains("Submitted user is not valid: PASSWORD_NOT_VALID")))
        ;

        Mockito.verify(roleRepository, Mockito.never()).findById(ArgumentMatchers.anyLong());
        Mockito.verify(userRepository, Mockito.never()).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_userDoesNotExist(){
        UUID uuid = UUID.randomUUID();

        Mockito.when(userRepository.findByPublicId(uuid.toString())).thenReturn(Mono.empty());

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s",
            "roleId": "%s"
        }
        """, "username", "password", "First", "Last", "3");

        client.put().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(managerRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(s -> Assertions.assertTrue(s.contains("User doesn't exist")));

        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_providedRoleDoesNotExist(){
        UUID uuid = UUID.randomUUID();
        Long invalidRole = 5L;

        Mockito.when(roleRepository.findById(invalidRole)).thenReturn(Mono.empty());
        Mockito.when(userRepository.findByPublicId(uuid.toString())).thenReturn(Mono.just(new User()));

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "firstName": "%s",
            "lastName": "%s",
            "roleId": "%s"
        }
        """, "username", "password", "First", "Last", invalidRole);

        client.put().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(managerRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(s -> Assertions.assertTrue(s.contains("Role provided does not exist")));

        Mockito.verify(roleRepository, Mockito.times(1)).findById(ArgumentMatchers.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_invalidAuthorities(){
        UUID uuid = UUID.randomUUID();
        client.put().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(userRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isForbidden();

        Mockito.verify(roleRepository, Mockito.never()).findById(ArgumentMatchers.anyLong());
        Mockito.verify(userRepository, Mockito.never()).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void patchUser(){
        UUID uuid = UUID.randomUUID();
        Long id = 5L;
        String username = "username";
        String password = "password";
        String firstName = "Tom";
        String changedLastName = "patched";

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setPublicId(uuid);
        user.setFirstName(firstName);
        user.setLastName("Last");

        Mockito.when(userRepository.findByPublicId(uuid.toString())).thenReturn(Mono.just(user));
        final ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userRepository.save(userArgumentCaptor.capture())).thenReturn(Mono.just(user));

        // WORKAROUND because of - password is not written by BodyInserters because @JsonProperty(Access.WRITE_ONLY) is used on entity
        String body = String.format("""
        {
            "username": "%s",
            "password": "%s",
            "lastName": "%s"
        }
        """, "changedUsername", "password", changedLastName);

        client.patch().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(managerRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isNoContent();

        Assertions.assertNotNull(userArgumentCaptor.getValue());
        Assertions.assertEquals(user.getId(), userArgumentCaptor.getValue().getId(), "ID should not be changed");
        Assertions.assertEquals(user.getPublicId(), uuid, "publicId should not be changed");
        Assertions.assertEquals(username, userArgumentCaptor.getValue().getUsername(), "Username should not be changed");
        Assertions.assertEquals(encodedPassword, userArgumentCaptor.getValue().getPassword(), "Password should be encoded");
        Assertions.assertEquals(changedLastName, userArgumentCaptor.getValue().getLastName(), "Last name should change");
        Assertions.assertEquals(firstName, userArgumentCaptor.getValue().getFirstName(), "First name should not be changed");

        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(uuid.toString());
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void patchUser_notAuth(){
        UUID uuid= UUID.randomUUID();
        client.patch().uri("/api/v1/users/"+uuid)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isUnauthorized();

        Mockito.verify(userRepository, Mockito.never()).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).findByPublicId(uuid.toString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void patchUser_userDoesNotExist(){
        UUID uuid = UUID.randomUUID();
        Mockito.when(userRepository.findByPublicId(uuid.toString())).thenReturn(Mono.empty());

        client.patch().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(managerRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(s -> Assertions.assertTrue(s.contains("User doesn't exist")));

        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByPublicId(uuid.toString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void patchUser_invalidAuthorities(){
        UUID uuid= UUID.randomUUID();
        client.patch().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(userRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new User()))
                .exchange()
                .expectStatus().isForbidden();

        Mockito.verify(userRepository, Mockito.never()).findByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).findByPublicId(uuid.toString());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void deleteUser(){
        UUID uuid = UUID.randomUUID();

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mono<Void> voidReturn = Mono.empty();
        Mockito.when(userRepository.deleteByPublicId(stringArgumentCaptor.capture())).thenReturn(voidReturn);

        client.delete().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(adminRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Assertions.assertEquals(uuid.toString(), stringArgumentCaptor.getValue());

        Mockito.verify(userRepository, Mockito.times(1)).deleteByPublicId(ArgumentMatchers.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).deleteByPublicId(uuid.toString());
    }

    @Test
    void deleteUser_notAuth(){
        UUID uuid= UUID.randomUUID();

        Mono<Void> voidReturn = Mono.empty();
        Mockito.when(userRepository.deleteByPublicId(uuid.toString())).thenReturn(voidReturn);

        client.delete().uri("/api/v1/users/"+uuid)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();

        Mockito.verify(userRepository, Mockito.never()).deleteByPublicId(ArgumentMatchers.anyString());
    }

    @Test
    void deleteUser_invalidAuthorities(){
        UUID uuid= UUID.randomUUID();

        Mono<Void> voidReturn = Mono.empty();
        Mockito.when(userRepository.deleteByPublicId(uuid.toString())).thenReturn(voidReturn);

        client.delete().uri("/api/v1/users/"+uuid)
                .headers(http -> http.setBearerAuth(userRoleToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();

        Mockito.verify(userRepository, Mockito.never()).deleteByPublicId(ArgumentMatchers.anyString());
    }
}
