package com.example.demo.repository;

import com.example.demo.Authority;
import com.example.demo.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    @Query(value = "SELECT a.id, a.authority_name FROM authority a INNER JOIN role_authority r ON r.role_id = :id AND r.authority_id = a.id")
    Flux<Authority> getAuthoritiesByRoleId(Long id);
}
