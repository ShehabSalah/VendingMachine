package com.flapkap.challenge.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flapkap.challenge.entities.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private int deposit;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

}
