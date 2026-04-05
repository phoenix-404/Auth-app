package com.example.auth_app_backend.config;


import com.example.auth_app_backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
public class SecurityConfig {

    // Custom filter that intercepts requests to check for a valid JWT in the headers
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. Disable CSRF (Cross-Site Request Forgery) as JWTs are inherently immune to most CSRF attacks
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Enable CORS (Cross-Origin Resource Sharing) with default settings to allow frontend communication
                .cors(Customizer.withDefaults())

                // 3. Set session management to STATELESS; the server won't store user state (no JSESSIONID)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Define authorization rules for specific URL patterns
                .authorizeHttpRequests(authHttpRequests ->
                        authHttpRequests
                                .requestMatchers("/api/v1/auth/register").permitAll() // Allow public access to registration
                                .requestMatchers("/api/v1/auth/login").permitAll()    // Allow public access to login
                                .anyRequest().authenticated()                         // All other endpoints require a valid login
                )

                // 5. Custom Error Handling: Triggered when an unauthenticated user tries to access a protected route
                .exceptionHandling( ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    authException.printStackTrace();

                    // Manually build a JSON error response instead of redirecting to a login page
                    response.setStatus(401);
                    response.setContentType("application/json");
                    String message = authException.getMessage();

                    Map<String,String> errorMap = Map.of(
                            "Message", message,
                            "statusCode", Integer.toString(401)
                    );

                    // Convert the Map to a JSON string and write it to the response body
                    var objectmapper = new ObjectMapper();
                    response.getWriter().write(objectmapper.writeValueAsString(errorMap));
                }))

                // 6. Register the JWT Filter BEFORE the standard Username/Password filter
                // This ensures we check for a token before Spring tries to look for a session or basic auth
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Defines the hashing algorithm for passwords.
     * When a user registers, their password will be hashed using BCrypt before saving to the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }
}

//    In-Memory Authentication
//    Spring Security’s InMemoryUserDetailsManager implements UserDetailsService to provide support for username/password
//    based authentication that is stored in memory. InMemoryUserDetailsManager provides management of UserDetails by implementing
//    the UserDetailsManager interface. UserDetails-based authentication is used by Spring Security when it is configured to accept a
//    username and password for authentication.
//    @Bean
//    public UserDetailsService users(){
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//        UserDetails user1= userBuilder.username("sourav").password("abc123").roles("ADMIN").build();
//        UserDetails user2= userBuilder.username("shreya").password("xyz123").roles("USER").build();
//        return new InMemoryUserDetailsManager(user1,user2);
//    }


