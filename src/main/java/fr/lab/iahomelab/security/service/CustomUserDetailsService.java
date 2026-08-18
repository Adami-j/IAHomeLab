package fr.lab.iahomelab.security.service;

import fr.lab.iahomelab.security.entity.AppUser;
import fr.lab.iahomelab.security.entity.UserIdentity;
import fr.lab.iahomelab.security.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserIdentityRepository userIdentityRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserIdentity identity = userIdentityRepository
                .findByProviderAndProviderSubject("local", username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        AppUser user = identity.getUser();

        return User.builder()
                .username(user.getUsername())
                .password(identity.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}