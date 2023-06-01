package com.sambath.security.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_email_unique",
                        columnNames = "email"
                )
        }
)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    @NotBlank
    private String name;

    private String firstName;
    private String lastName;

    @NotNull
    @NotBlank
    @Email
    private String email;

    private String password;
    private String imageUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)

    private AuthProvider provider;
    private String providerId;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name())); // Return a list of granted authorities
    }

    @Override
    public String getPassword() {
        return password; // Return password
    }

    @Override
    public String getUsername() {
        return email; // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account is not expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials are not expired
    }

    @Override
    public boolean isEnabled() {
        return enabled; // Account is enabled or not
    }
}
