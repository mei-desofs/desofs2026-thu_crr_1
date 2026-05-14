package com.techstore.app.service;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.repository.UserRepository;
import com.techstore.app.service.interfaces.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(String supabaseUserId, String email, String role) {
        Email emailVO = new Email(email);

        if (userRepository.findByEmail(emailVO).isPresent()) {
            throw new SecurityException("Email is already in use");
        }

        SupabaseUserId sid = SupabaseUserId.fromString(supabaseUserId);

        if (userRepository.existsBySupabaseUserId(sid)) {
            return userRepository.findBySupabaseUserId(sid).orElseThrow();
        }

        User user = new User(emailVO, Role.fromString(role), sid);
        return userRepository.save(user);
    }
}
