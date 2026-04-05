package com.example.auth_app_backend.security;

import com.example.auth_app_backend.helpers.UserHelper;
import com.example.auth_app_backend.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Look for the 'Authorization' header in the incoming request
        String header = request.getHeader("Authorization");
        logger.info("Authorization header : {}", header);

        // 2. Check if the header exists and starts with "Bearer " (standard JWT prefix)
        if(header != null && header.startsWith("Bearer ")){

            // Extract the actual token string (removing "Bearer " which is 7 characters)
            String token = header.substring(7);

            try {
                // 3. Security Check: Ensure this is an Access Token, not a Refresh Token.
                // You don't want users using long-lived Refresh Tokens to access data directly.
                if(!jwtService.isAccessToken(token)){
                    filterChain.doFilter(request, response);
                    return;
                }

                // 4. Validate the signature and get the data (claims) inside the token
                Jws<Claims> parse = jwtService.parse(token);
                Claims payload = parse.getPayload();

                // 5. Identify the user by the ID stored in the 'subject' field
                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUUID(userId);

                // 6. Look up the user in the database to ensure they still exist
                userRepository.findById(userUuid)
                        .ifPresent(user -> {

                            // 7. Check if the user account is active/enabled
                            if(user.isEnable()){
                                // Convert our DB roles into Spring Security 'GrantedAuthority' objects
                                List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() :
                                        user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(role.getName()))
                                                .collect(Collectors.toUnmodifiableList());

                                // 8. Create an Authentication object (The "Identity Card")
                                // We pass: Principal (email), Credentials (null because they already logged in), and Authorities
                                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authorities
                                );

                                // Add extra details like IP address/Session ID to the token
                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                // 9. THE MOST IMPORTANT STEP:
                                // If no one is currently authenticated, put this "Identity Card" into the Security Context.
                                // Now, the rest of the app knows exactly WHO is making this request.
                                if(SecurityContextHolder.getContext().getAuthentication() == null)
                                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            }
                        });

            } catch(ExpiredJwtException | MalformedJwtException e) {
                // Log issues if the token is old or tampered with
                logger.error("JWT Error: {}", e.getMessage());
            } catch(Exception e) {
                logger.error("Authentication Error: {}", e.getMessage());
            }
        }

        // 10. Continue the journey to the next filter or the Controller
        filterChain.doFilter(request, response);
    }
}
