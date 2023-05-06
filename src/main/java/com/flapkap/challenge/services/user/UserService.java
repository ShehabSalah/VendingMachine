package com.flapkap.challenge.services.user;

import com.flapkap.challenge.dto.user.JwtResponseDTO;
import com.flapkap.challenge.dto.user.LoginRequestDTO;
import com.flapkap.challenge.dto.user.UserDTO;
import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.exceptions.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Get all users
     *
     * @param page pagination information
     * @return a list of all users {@link UserDTO}
     * */
    Page<UserDTO> getAllUsers(Pageable page);

    /**
     * Login a user
     *
     * @param loginRequestDTO object that contains the username and password
     * @return the logged-in user with a JWT token {@link JwtResponseDTO}
     * @throws BadRequestException if the user does not exist or the password is incorrect
     * */
    JwtResponseDTO loginUser(LoginRequestDTO loginRequestDTO) throws BadRequestException;

    /**
     * Create a new user
     *
     * @param user object that contains the user information
     * @return the created user {@link UserDTO}
     * @throws BadRequestException if the user already exists
     * */
    UserDTO createUser(User user) throws BadRequestException;

    /**
     * This method is used to get the current authenticated user
     *
     * @return the current authenticated user {@link UserDTO}
     * @throws EntityNotFoundException if no user is authenticated
     */
    User getCurrentUser() throws EntityNotFoundException;

    /**
     * get a user by id
     *
     * @param id the id of the user
     * @return the user {@link UserDTO}
     * @throws EntityNotFoundException if the user does not exist
     * */
    UserDTO getUserById(Long id) throws EntityNotFoundException;

    /**
     * Update a user
     *
     * @param id the user id
     * @param user object that contains the user information
     * @return the updated user {@link UserDTO}
     * @throws BadRequestException if the user information is invalid
     * @throws EntityNotFoundException if the user does not exist
     * */
    UserDTO updateUser(Long id, User user) throws BadRequestException, EntityNotFoundException;

    /**
     * Delete a user
     *
     * @param id the id of the user
     * @throws EntityNotFoundException if the user does not exist
     * */
    void deleteUser(Long id) throws EntityNotFoundException;

    /**
     * Deposit money to a user account with role USER.
     * The user can deposit 5, 10, 20, 50 and 100 cent coins.
     *
     * @param amount the amount of money to deposit
     *               the amount must be 5, 10, 20, 50 or 100
     *               otherwise a BadRequestException will be thrown
     * @throws BadRequestException if the amount is invalid
     * @throws EntityNotFoundException if the user does not exist
     * */
    void depositMoney(Integer amount) throws BadRequestException, EntityNotFoundException;

    /**
     * Reset the user account balance to 0
     *
     * @throws EntityNotFoundException if the user does not exist
     * */
    void resetDeposit() throws EntityNotFoundException;

}
