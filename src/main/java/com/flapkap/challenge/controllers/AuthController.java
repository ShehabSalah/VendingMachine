package com.flapkap.challenge.controllers;

import com.flapkap.challenge.dto.user.JwtResponseDTO;
import com.flapkap.challenge.dto.user.LoginRequestDTO;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    /**
     * This endpoint logs in a user to the application.
     *
     * @param loginRequestDTO the user information used for login username and password
     * @return the logged-in user with a JWT token {@link JwtResponseDTO}
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) throws BadRequestException {
        log.debug("API ---> (/api/auth/login) has been called.");
        log.debug("Method Location: {}", this.getClass().getName() + ".login()");
        log.debug("Request body: {}", loginRequestDTO);
        return ResponseEntity.ok(userService.loginUser(loginRequestDTO));
    }
}
