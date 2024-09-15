package com.emce.brokage.auth;

import com.emce.brokage.auth.dto.AuthRequest;
import com.emce.brokage.auth.dto.AuthResponse;
import com.emce.brokage.auth.dto.RegisterRequest;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.auth.entity.Role;
import com.emce.brokage.exception.DuplicateEmailException;
import com.emce.brokage.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String TOKEN_EXPIRED_MSG = "Token is expired or invalid";
    public static final String EMAIL_ALREADY_EXISTS_MSG = "Email already exists! mail: %s";
    public static final String TOKEN_NOT_VALID = "Token cannot be validated";
    private final CustomerRepository customerRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    public AuthResponse register(RegisterRequest registerRequest) throws DataIntegrityViolationException {
        var userCredential = Customer.builder()
                .name(registerRequest.name())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.USER)
                .accountBalance(0D)
                .build();
        try {
            customerRepository.save(userCredential);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException(String.format(EMAIL_ALREADY_EXISTS_MSG, userCredential.getEmail()));
        }
        var jwtToken = jwtUtil.generateToken(userCredential);
        return AuthResponse.builder()
                .token(jwtToken)
                .expiresAt(jwtUtil.extractExpiration(jwtToken))
                .build();
    }

    public AuthResponse login(AuthRequest request) throws AuthenticationException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        var user = userService.loadUserByUsername(request.email());

        var jwtToken = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .expiresAt(jwtUtil.extractExpiration(jwtToken))
                .build();
    }

    public Integer validateToken(String token) throws ExpiredJwtException {
        final String userEmail = jwtUtil.extractUsername(token);

        if (userEmail != null) {
            Customer customer = this.userService.loadUserByUsername(userEmail);
            if (!jwtUtil.isTokenValid(token, customer)) {
                throw new CredentialsExpiredException(TOKEN_EXPIRED_MSG);
            }else {
                return customer.getId();
            }
        }
        throw new BadCredentialsException(TOKEN_NOT_VALID);
    }

}
