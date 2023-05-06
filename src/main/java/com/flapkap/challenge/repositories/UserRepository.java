package com.flapkap.challenge.repositories;

import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.entities.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByUsername(String username);
    List<User> findByRole(UserRole role);
    Optional<User> findByUsername(String username);

}
