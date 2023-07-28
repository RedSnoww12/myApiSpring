package com.etna.myapi.config;

import com.etna.myapi.entity.User;
import com.etna.myapi.services.jwt.JwtServiceInterface;
import com.etna.myapi.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtServiceInterface jwtService;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //authenticationManager.authenticate(
        //         new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        //);

        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(request.getUsername()));
        if (user.isPresent()) {
            String jwtToken = jwtService.generateToken(user.get());
            return AuthenticationResponse.builder()
                    .message("OK")
                    .token(jwtToken)
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + request.getUsername());
        }

    }
}
