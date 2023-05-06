package com.flapkap.challenge.controllers;

import com.flapkap.challenge.dto.ResponseDTO;
import com.flapkap.challenge.dto.user.UserDTO;
import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.exceptions.EntityNotFoundException;
import com.flapkap.challenge.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    /**
     * Get all users
     *
     * @param page the pagination information
     * @return the list of users {@link UserDTO}
     * */
    @GetMapping("/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers(Pageable page) {
        log.info("API ---> (/api/v1/users) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".getAllUsers()");
        return ResponseEntity.ok(userService.getAllUsers(page));
    }

    /**
     * Create a New User (shouldn’t require authentication)
     *
     * @param user the user information to be created
     * @return the created user {@link UserDTO}
     * @throws BadRequestException if the user already exists
     * */
    @PostMapping("/")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) throws BadRequestException {
        log.info("API ---> (/api/v1/users) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".createUser()");
        log.info("Request body: {}", user);
        UserDTO userDTO = userService.createUser(user);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/users/" + userDTO.getId()).toUriString());
        return ResponseEntity.created(uri).body(
                ResponseDTO.builder()
                        .message("User has been created successfully")
                        .data(user)
                        .build()
        );
    }

    /**
     * Update a user
     *
     * @param id the user id
     * @param user the user information to be updated
     * @return the updated user {@link UserDTO}
     * @throws BadRequestException if the user does not exist
     * */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user)
            throws BadRequestException, EntityNotFoundException {
        log.info("API ---> (/api/v1/users) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".updateUser()");
        log.info("Request body: {}", user);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("User has been updated successfully")
                        .data(userService.updateUser(id, user))
                        .build()
        );
    }

    /**
     * Delete a user
     *
     * @param id the user id
     * @return the deleted user {@link UserDTO}
     * @throws BadRequestException if the user does not exist
     * */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) throws BadRequestException {
        log.info("API ---> (/api/v1/users/{userId}) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".deleteUser()");
        log.info("Request body: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("User has been deleted successfully")
                        .build()
        );
    }

    /**
     * Get user details by user id
     *
     * @param id the user id
     * @return the user details {@link UserDTO}
     * @throws BadRequestException if the user does not exist
     * */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) throws BadRequestException {
        log.info("API ---> (/api/v1/users/{userId}) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".getUserById()");
        log.info("Request body: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * return the current authenticated user
     *
     * @return current authenticated {@link UserDTO}
     * @throws EntityNotFoundException if no user is authenticated
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER') or hasRole('ROLE_BUYER')")
    public ResponseEntity<?> getUserInfo() throws EntityNotFoundException {
        log.info("API ---> (/api/v1/users/userinfo) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".getUserInfo()");
        return ResponseEntity.ok(userService.getCurrentUser().toDTO());
    }

    /**
     * update the current authenticated user
     *
     * @param user the user information to be updated
     * @return the updated user {@link UserDTO}
     * @throws BadRequestException if the user trying to update another user
     * */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER') or hasRole('ROLE_BUYER')")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody User user) throws BadRequestException {
        log.info("API ---> (/api/v1/users/userinfo) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".updateUserInfo()");
        log.info("Request body: {}", user);

        User currentUser = userService.getCurrentUser();
        user.setRole(null); // to prevent the user from changing his role

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("User has been updated successfully")
                        .data(userService.updateUser(currentUser.getId(), user))
                        .build()
        );
    }

    /**
     * Deposit money to the current authenticated user
     *
     * @param amount the amount of money to be deposited
     * @throws BadRequestException if the amount is invalid
     * @throws EntityNotFoundException if the user does not exist
     * */
    @PutMapping("/deposit/{amount}")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> depositMoney(@PathVariable Integer amount) throws BadRequestException, EntityNotFoundException {
        log.info("API ---> (/api/v1/users/deposit/{amount}) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".depositMoney()");
        log.info("Request body: {}", amount);
        userService.depositMoney(amount);
        log.info("Money has been deposited successfully");
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("Money has been deposited successfully")
                        .build()
        );
    }

    /**
     * Reset the current authenticated user deposit amount to zero
     *
     * @throws EntityNotFoundException if the user does not exist
     * */
    @PutMapping("/reset")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> resetDeposit() throws EntityNotFoundException {
        log.info("API ---> (/api/v1/users/reset) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".resetDeposit()");
        userService.resetDeposit();
        log.info("Deposit amount has been reset successfully");
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("Deposit amount has been reset successfully")
                        .build()
        );
    }
}
