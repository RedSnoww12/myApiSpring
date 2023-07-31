package com.etna.myapi.controller.impl;


import com.etna.myapi.controller.UserControllerInterface;
import com.etna.myapi.dataobjects.mappers.UserObjectMapper;
import com.etna.myapi.dto.*;
import com.etna.myapi.entity.User;
import com.etna.myapi.security.CustomUserDetailsService;
import com.etna.myapi.security.JWTGenerator;
import com.etna.myapi.services.jwt.JwtServiceInterface;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.etna.myapi.controller.UserControllerInterface.ROOT_INTERFACE;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
@RequestMapping(path = ROOT_INTERFACE)
public class UserControllerImpl implements UserControllerInterface {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private UserObjectMapper userObjectMapper;

    @Autowired
    JwtServiceInterface jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    JWTGenerator jwtGenerator;


    public ResponseEntity<?> createUser(UserDto userDto) {
        try {

            if (userDto == null) {
                log.warn("userDto est null");
                return null;
            }
            if (userDto.getUsername() == null || userDto.getUsername().isEmpty()) {
                log.warn("userDto.username est null");
                return null;
            }
            if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
                log.warn("userDto.email est null");
                return null;
            }
            if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
                log.warn("userDto.password est null");
                return null;
            }

            log.debug("user.username: {}", userDto.getUsername());

            log.info("recupération d'un userDTO: {}", userDto);

            // hash password
            String hashedPassword = passwordEncoder.encode(userDto.getPassword());

            // create User
            User user = new User().toBuilder()
                    .username(userDto.getUsername())
                    .email(userDto.getEmail())
                    .password(hashedPassword)
                    .pseudo(userDto.getPseudo())
                    .created_at(Date.from(Instant.now()))
                    .build();

            log.debug("user: {}", user);

            userRepository.save(user);

            UserResponseDto userCreatedResponseDto =
                    new UserResponseDto().toBuilder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .pseudo(user.getPseudo())
                            .created_at(user.getCreated_at())
                            .build();

            // return ResponseSuccessDto
            ResponseSuccessDto responseSuccessDto =
                    new ResponseSuccessDto().toBuilder()
                            .message("Ok")
                            .data(userCreatedResponseDto)
                            .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(responseSuccessDto);
        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());
        }
        return null;
    }

    public ResponseEntity<?> allUsers(Optional<String> pseudo, int page, int perPage) {
        boolean isPseudo = true;
        Page<User> users = null;
        if (page <= 0) {
            return null;
        }
        if (perPage <= 0) {
            return null;
        }
        if (pseudo.isEmpty() || pseudo.get().isEmpty()) {
            isPseudo = false;
        }
        if (isPseudo) {
            log.debug("retrieve with pseudo");
            users = userService.getAllUser(page - 1, perPage, pseudo.get());
        } else {
            log.debug("retrieve without pseudo");
            users = userService.getAllUser(page - 1, perPage);
            log.debug("users: {}", users.get().collect(Collectors.toList()));
        }

        // if page > total page
        if (page > users.getTotalPages() && users.getTotalPages() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "Page out of range",
                                    "totalPages", String.valueOf(users.getTotalPages())
                            )
                    )
            );
        }

        UsersPageResponseDto usersPageResponseDto = new UsersPageResponseDto().toBuilder()
                .message("ok")
                .data(users.get()
                        .map(
                                user -> userObjectMapper.toCreatedResponseDto(user)
                        )
                        .collect(Collectors.toList()))
                .pager(new PageDto().toBuilder()
                        .current(users.getNumber() + 1)
                        .total(users.getTotalPages())
                        .build()
                )
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(usersPageResponseDto);
    }

    @Override
    public ResponseEntity<?> getUser(Integer id) {
        return ResponseEntity.ok("getUser");
    }

    @Override
    public ResponseEntity<?> deleteUser(Integer id) {
        try {
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            log.debug("login: {}", login);

            Optional<User> ouser = userRepository.findById(id);

            if (ouser.isEmpty()) {
                /*
                *   {
                        "message": "Not found"
                    }
                * */
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(
                        Map.of("message", "Not found")
                ));
            }

            User user = ouser.get();

            String userUsername = user.getUsername();
            String userEmail = user.getEmail();


            if (!userUsername.equals(login)
                    && !userEmail.equals(login)) {

                log.debug("userUsername: {}", userUsername);
                log.debug("userEmail: {}", userEmail);
                log.debug("login: {}", login);

                log.debug("compare with username: {}", userUsername.equals(login));
                log.debug("compare with email: {}", userEmail.equals(login));

                /*
                *   {
                        "message": "Forbidden"
                    }
                * */
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<>(
                        Map.of("message", "Forbidden")
                ));
            }

            // delete user in db
            userRepository.delete(user);

            // Return 204
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> authenticate(LoginDto request) {
        try {
            log.debug("authenticate: {}", request);
            log.debug("authenticate: {}", request.getLogin());
            log.debug("authenticate: {}", request.getPassword());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword()));

            log.debug("authenticate: {}", authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            //UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());

            String token = jwtGenerator.generateToken(authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDto("OK", token));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto("Username or password incorrect.", null));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto("Login or password incorrect.", null));
        } catch (Exception e) {
            log.debug("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto(e.getMessage(), null));
        }

    }

}