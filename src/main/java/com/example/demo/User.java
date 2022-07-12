package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_entity")
public class User {
    @Id  //import org.springframework.data.annotation
    private Integer id;
    private UUID publicId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
