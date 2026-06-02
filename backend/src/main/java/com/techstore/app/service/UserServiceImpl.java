package com.techstore.app.service;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.UserRepository;
import com.techstore.app.service.interfaces.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public UserServiceImpl(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    @Override
    public User registerUser(String supabaseUserId, String email, String role) {
        Email emailVO = new Email(email);
        Role userRole = Role.fromString(role);

        if (userRepository.findByEmail(emailVO).isPresent()) {
            throw new SecurityException("Email is already in use");
        }

        SupabaseUserId sid = SupabaseUserId.fromString(supabaseUserId);

        if (userRepository.existsBySupabaseUserId(sid)) {
            User existingUser = userRepository.findBySupabaseUserId(sid).orElseThrow();
            ensureCustomerExists(existingUser);
            return existingUser;
        }

        User user = new User(emailVO, userRole, sid);
        User savedUser = userRepository.save(user);
        ensureCustomerExists(savedUser);
        return savedUser;
    }

    private void ensureCustomerExists(User user) {
        if (user.getRole() != Role.CUSTOMER || customerRepository.existsByUser(user)) {
            return;
        }

        customerRepository.save(new Customer(user));
    }

    public java.util.Optional<User> getUserBySupabaseId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));
    }

    public User confirmUserEmail(String supabaseUserId) {
        var user = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                .orElseThrow(() -> new BusinessException("User not found"));
        user.markEmailAsValidated();
        return userRepository.save(user);
    }
}
