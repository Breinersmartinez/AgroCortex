package com.agrocortex.auth.infrastructure.security;

import com.agrocortex.auth.application.ports.out.UserRepository;
import com.agrocortex.auth.domain.Permission;
import com.agrocortex.auth.domain.Role;
import com.agrocortex.auth.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Service
public final class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            user = userRepository.findById(UUID.fromString(username))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Invalid user subject", ex);
        }

        if (!user.active()) {
            throw new UsernameNotFoundException("User inactive");
        }

        return new org.springframework.security.core.userdetails.User(
                user.email(),
                user.passwordHash(),
                user.active(),
                true,
                true,
                true,
                buildAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        for (Role role : user.roles()) {
            if (!role.active()) {
                continue;
            }

            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.code()));

            for (Permission permission : role.permissions()) {
                if (permission.active()) {
                    authorities.add(new SimpleGrantedAuthority(permission.code()));
                }
            }
        }

        return authorities;
    }
}
