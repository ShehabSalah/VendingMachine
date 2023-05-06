package com.flapkap.challenge.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponseDTO {
    private UserDTO user;
    private String token;
    @Builder.Default
    private String type = "Bearer";
}
