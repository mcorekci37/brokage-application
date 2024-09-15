package com.emce.brokage.auth;

import com.emce.brokage.auth.dto.AuthRequest;
import com.emce.brokage.auth.dto.AuthResponse;
import com.emce.brokage.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest registerRequest){
        return new ResponseEntity<>(authService.register(registerRequest), HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/validate")
    public ResponseEntity<Integer> validateToken(@RequestParam("token") String token) {
        Integer userId = authService.validateToken(token);
        return ResponseEntity.ok(userId);
    }
}
