package com.flapkap.challenge.services.user;

import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.repositories.UserRepository;
import com.flapkap.challenge.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // check if the username exists in the user table
        Optional<User> user = userRepository.findOneByUsername(username);

        if (user.isPresent()) {
            return UserPrincipal.build(user.get());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

}
