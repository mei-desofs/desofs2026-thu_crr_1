package com.techstore.app.service;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUserShouldCreateCustomerForCustomerRole() {
        String supabaseUserId = "123e4567-e89b-12d3-a456-426614174000";
        String email = "user@example.com";
        Email emailVO = new Email(email);
        SupabaseUserId sid = SupabaseUserId.fromString(supabaseUserId);

        when(userRepository.findByEmail(emailVO)).thenReturn(Optional.empty());
        when(userRepository.existsBySupabaseUserId(sid)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerRepository.existsByUser(any(User.class))).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerUser(supabaseUserId, email, Role.CUSTOMER.toString());

        assertEquals(emailVO, user.getEmail());
        verify(userRepository).save(any(User.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void registerUserShouldNotCreateCustomerForManagerRole() {
        String supabaseUserId = "123e4567-e89b-12d3-a456-426614174001";
        String email = "manager@example.com";
        Email emailVO = new Email(email);
        SupabaseUserId sid = SupabaseUserId.fromString(supabaseUserId);

        when(userRepository.findByEmail(emailVO)).thenReturn(Optional.empty());
        when(userRepository.existsBySupabaseUserId(sid)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerUser(supabaseUserId, email, Role.MANAGER.toString());

        assertEquals(emailVO, user.getEmail());
        verify(userRepository).save(any(User.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }
}