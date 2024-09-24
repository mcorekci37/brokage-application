package com.emce.brokage.auth;

import com.emce.brokage.auth.dto.AuthRequest;
import com.emce.brokage.auth.dto.AuthResponse;
import com.emce.brokage.auth.dto.RegisterRequest;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.auth.entity.Role;
import com.emce.brokage.exception.DuplicateEmailException;
import com.emce.brokage.security.token.Token;
import com.emce.brokage.security.token.TokenRepository;
import com.emce.brokage.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import static com.emce.brokage.common.Messages.EMAIL_ALREADY_EXISTS_MSG;
import static com.emce.brokage.common.Messages.TOKEN_EXPIRED_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        // Given
        String jwtToken = "jwtToken";
        String testName = "TestName";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        RegisterRequest registerRequest =
                createSimpleRegisterRequest(testName, email);
        Customer customer = createSimpleCustomer(testName, email, encodedPassword);

        when(passwordEncoder.encode(registerRequest.password()))
                .thenReturn(encodedPassword);
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);
        when(jwtUtil.generateToken(customer))
                .thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken))
                .thenReturn(new Date(System.currentTimeMillis() + 6000000L));

        // when
        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(jwtToken, response.token());

        // then
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals(testName, customerCaptor.getValue().getName());
        assertEquals(email, customerCaptor.getValue().getEmail());

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals(jwtToken, tokenCaptor.getValue().getToken());
    }

    private static Customer createSimpleCustomer(String name, String email, String password) {
        return Customer.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(Role.USER)
                .build();
    }

    private static RegisterRequest createSimpleRegisterRequest(String testName, String email) {
        return new RegisterRequest(testName, email, "password123");
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        // Given
        String testName = "TestName";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        RegisterRequest registerRequest = createSimpleRegisterRequest(testName, email);
        String msg = String.format(EMAIL_ALREADY_EXISTS_MSG, email);

        when(passwordEncoder.encode(registerRequest.password()))
                .thenReturn(encodedPassword);

        when(customerRepository.save(any(Customer.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // when
        DuplicateEmailException thrownException = assertThrows(DuplicateEmailException.class, () -> {
            authService.register(registerRequest);
        });

        // then
        assertEquals(msg, thrownException.getMessage());
    }


    @Test
    void testLogin_Success() {
        // given
        String jwtToken = "jwtToken";
        String testName = "TestName";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        AuthRequest authRequest = new AuthRequest(email, "password123");
        Customer customer = createSimpleCustomer(testName, email, encodedPassword);

        when(userService.loadUserByUsername(authRequest.email()))
                .thenReturn(customer);
        when(jwtUtil.generateToken(customer))
                .thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken))
                .thenReturn(new Date(System.currentTimeMillis() + 6000000L));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        // when
        AuthResponse response = authService.login(authRequest);

        // then
        assertNotNull(response);
        assertEquals(jwtToken, response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        // given
        String email = "test@example.com";
        AuthRequest authRequest = new AuthRequest(email, "wrongpassword");

        doThrow(new CredentialsExpiredException(TOKEN_EXPIRED_MSG))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when
        CredentialsExpiredException exception = assertThrows(CredentialsExpiredException.class, () ->
                authService.login(authRequest));

        // then
        assertEquals(TOKEN_EXPIRED_MSG, exception.getMessage());
    }

    @Test
    void testValidateToken_Success() {
        // given
        String token = "validJwtToken";
        String testName = "TestName";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        Customer customer = createSimpleCustomer(testName, email, encodedPassword);
        customer.setId(1);

        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(userService.loadUserByUsername(email)).thenReturn(customer);
        when(jwtUtil.isTokenValid(token, customer)).thenReturn(true);

        // when
        Integer customerId = authService.validateToken(token);

        // then
        assertEquals(1, customerId);
        verify(jwtUtil).isTokenValid(token, customer);
    }

    @Test
    void testValidateToken_InvalidToken_ThrowsException() {
        // given
        String token = "invalidJwtToken";
        String testName = "TestName";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        Customer customer = createSimpleCustomer(testName, email, encodedPassword);

        // when
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(userService.loadUserByUsername(email)).thenReturn(customer);
        when(jwtUtil.isTokenValid(token, customer)).thenReturn(false);

        // then
        CredentialsExpiredException exception = assertThrows(CredentialsExpiredException.class, () -> authService.validateToken(token));
        assertEquals(TOKEN_EXPIRED_MSG, exception.getMessage());
    }
}
