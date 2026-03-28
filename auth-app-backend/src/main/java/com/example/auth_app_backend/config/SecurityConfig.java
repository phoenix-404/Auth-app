package com.example.auth_app_backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests(authorizeHttpRequests ->
                    authorizeHttpRequests.requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

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
