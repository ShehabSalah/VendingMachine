package com.flapkap.challenge.entities;

import com.flapkap.challenge.dto.user.UserDTO;
import com.flapkap.challenge.entities.base.BaseEntityAudit;
import com.flapkap.challenge.entities.enums.UserRole;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import javax.persistence.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name="users")
public class User extends BaseEntityAudit {
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    @Column(nullable = false)
    @Size(min = 6, max = 120, message = "Password must be between 6 and 40 characters")
    private String password;
    @Column(nullable = false)
    private int deposit = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public UserDTO toDTO() {
        return UserDTO.builder()
                .id(id)
                .username(username)
                .deposit(deposit)
                .role(role)
                .createdAt(createdAt)
                .lastModifiedAt(lastModifiedAt)
                .build();
    }
}
