package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.UserControllerInterface;
import com.etna.myapi.dataobjects.mappers.UserObjectMapper;
import com.etna.myapi.dto.*;
import com.etna.myapi.entity.User;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
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

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
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
            UserCreatedResponseDto userCreatedResponseDto =
                    new UserCreatedResponseDto().toBuilder()
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

}