package com.etna.myapi.security;

import com.etna.myapi.entity.User;
import com.etna.myapi.services.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        Boolean userFind = true;

        UserDetails userDetails = null;

        log.debug("Load user by username: " + login);

        User user = userRepository.findByUsername(login);

        if (user == null) {
            log.debug("User not found by username test email: " + login);
            user = userRepository.findByEmail(login);

            log.debug("user: " + user);

            if (user != null) {
                log.debug("User found by email: " + login);
                userFind = true;
                return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Collections.emptyList());
            } else {
                log.warn("User not found by email: " + login);
                userFind = false;
                throw new UsernameNotFoundException("User not found");
            }
        }
        log.info("User found by username: " + login);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Collections.emptyList());
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Email not found");
        }

        log.debug("User found by email: " + user.getEmail());

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }

    /*private Collection<GrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }*/
}
