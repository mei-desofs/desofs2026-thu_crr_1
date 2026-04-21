package com.techstore.app.service.interfaces;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.User;
import com.techstore.app.repository.interfaces.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(String supabaseUserId, String email, String role) {
        Email emailVO = new Email(email);
        if(userRepository.findByEmail(emailVO).isPresent()){
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User(emailVO, Role.fromString(role), UUID.fromString(supabaseUserId));

        return userRepository.save(user);
    }
}
