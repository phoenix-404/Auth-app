package com.example.auth_app_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_name")
    private UUID id;

    @Column(name = "user_email", unique = true)
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enable = true;
    private Instant createdAt = Instant.now(); //Obtains the current instant from the system clock in UTC
    private Instant updatedAt = Instant.now(); //Automatically handled by JPA and Hibernate.

//    private String gender;
//    private Address address;

    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;

//    Whenever We load a User from the database,
//    Spring will automatically grab all their Roles at the same time
//    (instead of waiting until you ask for them).
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
//  Set instead of a List ensures that a user cannot have the same role twice (no duplicates).
    private Set<Role> roles = new HashSet<>();

    //Entity life cycle
    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        if(createdAt == null)
            createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enable;
    }
}
