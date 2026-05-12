package com.techstore.app.repository;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UserId> {

    Optional<User> findByEmail(Email email);

    User save(User user);
}
