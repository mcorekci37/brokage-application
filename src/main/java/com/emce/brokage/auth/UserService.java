package com.emce.brokage.auth;

import com.emce.brokage.auth.dto.RegisterRequest;
import com.emce.brokage.auth.dto.UserResponse;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.exception.DuplicateEmailException;
import com.emce.brokage.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static final String ACCESS_DENIED_FOR_USER_MSG = "You do not have permission to update the user with id %s";
    public static final String EMAIL_ALREADY_EXISTS_MSG = "Email already exists! mail: %s";

    public static final String USER_NOT_FOUND_MSG = "User not found with email %s";
    public static final String USER_ID_NOT_FOUND_MSG = "User not found with id %s";
    private final CustomerRepository customerRepository;

    @Override
    public Customer loadUserByUsername(String username) throws UsernameNotFoundException {
        return customerRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
    }
    public Customer loadUserById(Integer id) throws UsernameNotFoundException {
        return customerRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MSG, id)));
    }
    public boolean checkExistsById(Integer id) {
        return customerRepository.existsById(id);
    }

    public UserResponse update(RegisterRequest registerRequest, Integer userId) {
        Customer customer = customerRepository.findByEmail(registerRequest.email()).
                orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, registerRequest.email())));
        if (customer.getId()!=userId){
            throw new AccessDeniedException(String.format(ACCESS_DENIED_FOR_USER_MSG, userId));
        }

        customer.setName(registerRequest.name());
        try {
            customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException(String.format(EMAIL_ALREADY_EXISTS_MSG, customer.getEmail()));
        }
        return UserResponse.fromEntity(customer);

    }
}
