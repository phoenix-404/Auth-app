package com.example.auth_app_backend.dtos;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserDTO userDTO
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserDTO userDTO){
        return new TokenResponse(accessToken,refreshToken,expiresIn,"Bearer",userDTO);
    }
}
