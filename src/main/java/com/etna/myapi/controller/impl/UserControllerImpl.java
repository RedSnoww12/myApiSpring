package com.etna.myapi.controller.impl;


import com.etna.myapi.controller.UserControllerInterface;
import com.etna.myapi.dataobjects.ResponseEntityBuilder;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Autowired
    private CustomUserDetailsService customUserDetailsService;



    public ResponseEntity<?> createUser(UserDto userDto) {
            if (userDto == null) {
                log.warn("userDto est null");
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);
            }
            if (userDto.getUsername() == null || userDto.getUsername().isEmpty()) {
                log.warn("userDto.username est null");
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);
            }
            if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
                log.warn("userDto.email est null");
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);
            }
            if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
                log.warn("userDto.password est null");
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);
            }

            log.debug("user.username: {}", userDto.getUsername());

        log.info("recupération d'un user: {}", userDto);

        // check if user already exist
        Optional<User> ouser = Optional.ofNullable(userRepository.findByUsername(userDto.getUsername()));
        if (ouser.isPresent()) {
            log.warn("user exist with this username");
            return new ResponseEntityBuilder()
                    .setData(List.of())
                    .buildBadRequest(10001);
        }

        ouser = Optional.ofNullable(userRepository.findByEmail(userDto.getEmail()));
        if (ouser.isPresent()) {
            log.warn("user exist with this email");
            return new ResponseEntityBuilder()
                    .setData(List.of())
                    .buildBadRequest(10001);
        }

        emailMatcher emailMatcher = getEmailMatcher(userDto.getEmail());
        if (!emailMatcher.matcher().matches()) {
            log.warn("email not valid");
            return new ResponseEntityBuilder()
                    .setData(List.of())
                    .buildBadRequest(10001);
        }

        usernameMatcher usernameMatcher = getUsernameMatcher(userDto.getUsername());
        if (!usernameMatcher.matcher().matches()) {
            log.warn("username not valid");
            return new ResponseEntityBuilder()
                    .setData(List.of())
                    .buildBadRequest(10001);
        }

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

        return new ResponseEntityBuilder().setData(userCreatedResponseDto).buildCreated();
    }

    public ResponseEntity<?> AllUser(Optional<String> pseudo, int page, int perPage) {
        boolean isPseudo = true;
        Page<User> users = null;
        if (page <= 0) {
            return new ResponseEntityBuilder()
                    .setData(List.of())
                    .buildBadRequest(10001);
        }
        if (perPage <= 0) {
            return new ResponseEntityBuilder()
                    .setData(List.of())
                    .buildBadRequest(10001);
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

        // sort users by id desc
        List<User> sortedUsers = users.get()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();

        // if page > total page
        if (page > users.getTotalPages() && users.getTotalPages() > 0) {
            return new ResponseEntityBuilder()
                    .setData(List.of("La page demandée n'existe pas"))
                    .buildBadRequest(10001);
        }

        List<UserResponseDto> usersResponseDto = sortedUsers.stream()
                .map(
                        user -> userObjectMapper.toCreatedResponseDto(user)
                )
                .toList();

        PageDto pager = new PageDto().toBuilder()
                .current(users.getNumber() + 1)
                .total(users.getTotalPages())
                .build();

        return new ResponseEntityBuilder()
                .setData(usersResponseDto)
                .setPager(pager)
                .buildOk();
    }

    @Override
    public ResponseEntity<?> getUserById(Integer id) {
        try{
            Optional<User> user = userRepository.findById(id);

            if (user.isEmpty()) {
                return new ResponseEntityBuilder().buildNotFound();
            }

            // Convert the user to UserResponseDto
            UserResponseDto userResponseDto = userObjectMapper.toCreatedResponseDto(user.get());

            return new ResponseEntityBuilder().setData(userResponseDto).buildOk();
        }catch (Exception e){
            log.warn("Error : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }


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
                return new ResponseEntityBuilder().buildNotFound();
            }

            User user = ouser.get();

            String userUsername = user.getUsername();
            String userEmail = user.getEmail();


            if (!userUsername.equals(login)
                    && !userEmail.equals(login)) {
                return new ResponseEntityBuilder().buildForbidden();
            }

            // delete user in db
            userRepository.delete(user);

            return new ResponseEntityBuilder().buildDeleted();

        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> authenticate(LoginDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword()));

            log.debug("authenticate: {}", authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            //UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());

            String token = jwtGenerator.generateToken(authentication);
            return new ResponseEntityBuilder().setData(token).buildCreated();

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto("Username or password incorrect.", null));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto("Login or password incorrect.", null));
        } catch (Exception e) {
            log.debug("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto(e.getMessage(), null));
        }

    }

    @Override
    public ResponseEntity<?> updateUser(Integer id, UserDto userDto) {
        try {
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            log.debug("login: {}", login);

            Optional<User> ouser = userRepository.findById(id);

            if (ouser.isEmpty()) {
                return new ResponseEntityBuilder().buildNotFound();
            }

            User user = ouser.get();

            String userUsername = user.getUsername();
            String userEmail = user.getEmail();

            if (!userUsername.equals(login)
                    && !userEmail.equals(login)) {
                return new ResponseEntityBuilder().buildForbidden();
            }

            // check if user already exist
            Optional<User> ouser2 = Optional.ofNullable(userRepository.findByUsername(userDto.getUsername()));
            if (ouser2.isPresent() && !userDto.getUsername().equals(user.getUsername())) {
                log.warn("user exist with this username");
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);
            }

            ouser2 = Optional.ofNullable(userRepository.findByEmail(userDto.getEmail()));
            if (ouser2.isPresent() && !userDto.getEmail().equals(user.getEmail())) {
                log.warn("user exist with this email");
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);
            }

            // hash password
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(userDto.getPassword());
                user.setPassword(hashedPassword);
            }

            if (userDto.getUsername() != null && !userDto.getUsername().isEmpty()) {
                usernameMatcher usernameMatcher = getUsernameMatcher(userDto.getUsername());
                if (!usernameMatcher.matcher().matches()) {
                    log.warn("username not valid");
                    return new ResponseEntityBuilder()
                            .setData(List.of())
                            .buildBadRequest(10001);
                }
                user.setUsername(userDto.getUsername());
            }

            if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
                emailMatcher emailMatcher = getEmailMatcher(userDto.getEmail());
                if (!emailMatcher.matcher().matches()) {
                    log.warn("email not valid");
                    return new ResponseEntityBuilder()
                            .setData(List.of())
                            .buildBadRequest(10001);
                }
                user.setEmail(userDto.getEmail());
            }

            if (userDto.getPseudo() != null && !userDto.getPseudo().isEmpty()) {
                user.setPseudo(userDto.getPseudo());
            }

            log.debug("user: {}", user);

            userRepository.save(user);

            UserResponseDto userCreatedResponseDto =
                    new UserResponseDto().toBuilder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .pseudo(user.getPseudo())
                            .created_at(user.getCreated_at())
                            .build();

            return new ResponseEntityBuilder().setData(userCreatedResponseDto).buildCreated();

        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }


    private static emailMatcher getEmailMatcher(String inputEmail) {
        String regexPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        String email = inputEmail;
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(email);
        return new emailMatcher(email, matcher);
    }

    private static usernameMatcher getUsernameMatcher(String inputUsername) {
        String regexPattern = "^[a-zA-Z0-9_-]+$";
        String username1 = inputUsername;
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(username1);
        return new usernameMatcher(username1, matcher);
    }

    private record emailMatcher(String email, Matcher matcher) {
    }

    private record usernameMatcher(String username1, Matcher matcher) {
    }

}