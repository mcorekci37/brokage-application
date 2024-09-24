package com.emce.brokage.auth;

import com.emce.brokage.auth.dto.RegisterRequest;
import com.emce.brokage.auth.dto.UserResponse;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.exception.DuplicateEmailException;
import com.emce.brokage.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import static com.emce.brokage.common.Messages.ACCESS_DENIED_FOR_USER_MSG;
import static com.emce.brokage.common.Messages.EMAIL_ALREADY_EXISTS_MSG;
import static com.emce.brokage.common.Messages.USER_ID_NOT_FOUND_MSG;
import static com.emce.brokage.common.Messages.USER_NOT_FOUND_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_shouldReturnCustomer_whenUserExists() {
        // Given
        String username = "test@example.com";
        Customer customer = new Customer();
        customer.setEmail(username);

        when(customerRepository.findByEmail(username)).thenReturn(Optional.of(customer));

        // When
        Customer result = userService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getEmail());
        verify(customerRepository, times(1)).findByEmail(username);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // Given
        String username = "nonexistent@example.com";

        when(customerRepository.findByEmail(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(username);
        });

        assertEquals(String.format(USER_NOT_FOUND_MSG, username), exception.getMessage());
        verify(customerRepository, times(1)).findByEmail(username);
    }

    @Test
    void loadUserById_shouldReturnCustomer_whenUserExists() {
        // Given
        Integer userId = 1;
        Customer customer = new Customer();
        customer.setId(userId);

        when(customerRepository.findById(userId)).thenReturn(Optional.of(customer));

        // When
        Customer result = userService.loadUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(customerRepository, times(1)).findById(userId);
    }

    @Test
    void loadUserById_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Given
        Integer userId = 1;

        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.loadUserById(userId);
        });

        assertEquals(String.format(USER_ID_NOT_FOUND_MSG, userId), exception.getMessage());
        verify(customerRepository, times(1)).findById(userId);
    }

    @Test
    void checkExistsById_shouldReturnTrue_whenUserExists() {
        // Given
        Integer userId = 1;

        when(customerRepository.existsById(userId)).thenReturn(true);

        // When
        boolean exists = userService.checkExistsById(userId);

        // Then
        assertTrue(exists);
        verify(customerRepository, times(1)).existsById(userId);
    }

    @Test
    void checkExistsById_shouldReturnFalse_whenUserDoesNotExist() {
        // Given
        Integer userId = 1;

        when(customerRepository.existsById(userId)).thenReturn(false);

        // When
        boolean exists = userService.checkExistsById(userId);

        // Then
        assertFalse(exists);
        verify(customerRepository, times(1)).existsById(userId);
    }

    @Test
    void update_shouldUpdateCustomer_whenValidRequestAndUserExists() {
        // Given
        String email = "test@example.com";
        Integer userId = 1;
        Customer customer = new Customer();
        customer.setId(userId);
        customer.setEmail(email);

        RegisterRequest request = new RegisterRequest("newName", email, "password");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        UserResponse response = userService.update(request, userId);

        // Then
        assertNotNull(response);
        assertEquals("newName", customer.getName());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void update_shouldThrowAccessDeniedException_whenUserIdsMismatch() {
        // Given
        String email = "test@example.com";
        Integer userId = 1;
        Integer differentUserId = 2;
        Customer customer = new Customer();
        customer.setId(differentUserId);
        customer.setEmail(email);

        RegisterRequest request = new RegisterRequest("newName", email, "password");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.update(request, userId);
        });

        assertEquals(String.format(ACCESS_DENIED_FOR_USER_MSG, userId), exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void update_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        // Given
        String email = "test@example.com";
        Integer userId = 1;
        Customer customer = new Customer();
        customer.setId(userId);
        customer.setEmail(email);

        RegisterRequest request = new RegisterRequest("newName", email, "password");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenThrow(new DataIntegrityViolationException("duplicate email"));

        // When & Then
        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> {
            userService.update(request, userId);
        });

        assertEquals(String.format(EMAIL_ALREADY_EXISTS_MSG, email), exception.getMessage());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
}
