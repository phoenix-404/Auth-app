package com.example.auth_app_backend.controllers;

import com.example.auth_app_backend.dtos.LoginRequest;
import com.example.auth_app_backend.dtos.TokenResponse;
import com.example.auth_app_backend.dtos.UserDTO;
import com.example.auth_app_backend.entities.User;
import com.example.auth_app_backend.repositories.UserRepository;
import com.example.auth_app_backend.security.JWTService;
import com.example.auth_app_backend.services.AuthService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest
    ){
        //authenticate
        Authentication authenticate = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid Username/Password"));
        if(!user.isEnable()){
            throw new DisabledException("User is disabled");
        }

        //generate token
        String accessToken = jwtService.generateAccessToken(user);

        TokenResponse tokenResponse = TokenResponse.of(accessToken,"",jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDTO.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Username/Password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }
}
