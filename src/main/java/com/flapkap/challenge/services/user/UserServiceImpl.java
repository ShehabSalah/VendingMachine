package com.flapkap.challenge.services.user;

import com.flapkap.challenge.dto.user.JwtResponseDTO;
import com.flapkap.challenge.dto.user.LoginRequestDTO;
import com.flapkap.challenge.dto.user.UserDTO;
import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.exceptions.EntityNotFoundException;
import com.flapkap.challenge.repositories.UserRepository;
import com.flapkap.challenge.security.JWTUtils;
import com.flapkap.challenge.security.UserPrincipal;
import com.flapkap.challenge.utils.AllowedPrices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public Page<UserDTO> getAllUsers(Pageable page) {
        return userRepository.findAll(page).map(User::toDTO);
    }

    @Override
    public JwtResponseDTO loginUser(LoginRequestDTO loginRequestDTO) throws BadRequestException {
        // find user by username
        User user = userRepository.findOneByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new BadRequestException("User does not exist"));

        // Generate JWT token
        String jwt = generateJWTToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        log.debug("Login User JWT: {}", jwt);

        // get the user DTO and remove the createdAt and lastModifiedAt fields
        UserDTO userDTO = user.toDTO();
        userDTO.setCreatedAt(null);
        userDTO.setLastModifiedAt(null);

        return JwtResponseDTO.builder()
                .user(userDTO)
                .token(jwt)
                .build();
    }

    @Override
    public UserDTO createUser(User user) throws BadRequestException {
        // check if the username already exists
        if (userRepository.findOneByUsername(user.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }

        if (user.getRole() == null) {
            throw new BadRequestException("Role is required");
        }

        // encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user).toDTO();
    }

    @Override
    public User getCurrentUser() throws EntityNotFoundException {
        // get the current authenticated user
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // check if the user is authenticated and exists, then return the user, else throw an exception
        return userRepository.findOneByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public UserDTO getUserById(Long id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")).toDTO();
    }

    @Override
    public UserDTO updateUser(Long id, User user) throws BadRequestException, EntityNotFoundException {
        // check if the user exists
        User existingUser = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BadRequestException("Username is required");
        }

        // check if the username already exists
        if (!existingUser.getUsername().equals(user.getUsername()) && userRepository.findOneByUsername(user.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        // set the username to the existing username
        existingUser.setUsername(user.getUsername());

        // check if the password is not empty
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // encode the password
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // check if the user role is not empty
        if (user.getRole() != null) {
            // set the role to the existing role
            existingUser.setRole(user.getRole());
        }

        // check if the deposit is not empty
        if (user.getDeposit() != 0) {
            // set the deposit to the existing deposit
            existingUser.setDeposit(user.getDeposit());
        }

        // save the user
        userRepository.save(existingUser);

        return existingUser.toDTO();
    }

    @Override
    public void deleteUser(Long id) throws EntityNotFoundException {
        // check if the user exists
        User existingUser = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));

        // delete the user
        userRepository.delete(existingUser);
    }

    @Override
    public void depositMoney(Integer amount) throws BadRequestException, EntityNotFoundException {
        // get the current authenticated user
        User existingUser = getCurrentUser();

        // log the username and user id
        log.info("User: {}", existingUser.getUsername());
        log.info("User id: {}", existingUser.getId());

        // check if the amount is not empty
        if (amount == null || amount == 0) {
            throw new BadRequestException("Amount is required");
        }

        // check if the amount is negative
        if (amount < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        // deposit must be 5, 10, 20, 50 and 100 cent coins only
        if (!AllowedPrices.isAllowedPrice(amount)) {
            throw new BadRequestException("Invalid amount. Please deposit 5, 10, 20, 50 or 100 cent coins.");
        }

        // log the amount
        log.info("Amount: {}", amount);

        // set the deposit to the existing deposit + the amount
        existingUser.setDeposit(existingUser.getDeposit() + amount);

        // log the new deposit
        log.info("New deposit: {}", existingUser.getDeposit());

        // save the user
        userRepository.save(existingUser);
    }

    @Override
    public void resetDeposit() throws EntityNotFoundException {
        // get the current authenticated user
        User existingUser = getCurrentUser();

        // log the username and user id
        log.info("User: {}", existingUser.getUsername());
        log.info("User id: {}", existingUser.getId());

        // set the deposit to 0
        existingUser.setDeposit(0);

        // log the new deposit
        log.info("New deposit: {}", existingUser.getDeposit());

        // save the user
        userRepository.save(existingUser);
    }

    /**
     * Generate a JWT token for the user
     *
     * @param username the user username
     * @param password the user password
     * @return the generated JWT token
     */
    private String generateJWTToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.generateJwtToken(authentication);
    }

}
