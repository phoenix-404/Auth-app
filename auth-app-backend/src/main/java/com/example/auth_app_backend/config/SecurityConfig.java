package com.example.auth_app_backend.config;


import com.example.auth_app_backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authHttpRequests ->
                    authHttpRequests.requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
//                .httpBasic(Customizer.withDefaults());
                .exceptionHandling( ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    //error message
                    authException.printStackTrace();
                    response.setStatus(401);
                    response.setContentType("application/json");
                    String message = "unauthorised access "+authException.getMessage();
                    Map<String,String> errorMap = Map.of("Message",message,"Status",String.valueOf(401),"statusCode", Integer.toString(401));
                    var objectmapper = new ObjectMapper();
                    response.getWriter().write(objectmapper.writeValueAsString(errorMap));
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
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
}

