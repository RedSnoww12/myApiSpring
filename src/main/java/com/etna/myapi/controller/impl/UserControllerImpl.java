package com.etna.myapi.controller.impl;

import com.etna.myapi.User;
import com.etna.myapi.controller.UserControllerInterface;
import com.etna.myapi.dto.ResponseSuccessDto;
import com.etna.myapi.dto.UserDto;
import com.etna.myapi.services.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;

import static com.etna.myapi.controller.UserControllerInterface.ROOT_INTERFACE;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
@RequestMapping(path = ROOT_INTERFACE)
public class UserControllerImpl implements UserControllerInterface {
    @Autowired
    private UserRepository userRepository;


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

            // create User
            User user = new User().toBuilder()
                    .username(userDto.getUsername()) // TODO: Une erreur ici ? "username is marked non-null but is null"
                    .email(userDto.getEmail())
                    .password(userDto.getPassword()) // TODO: Hasher le password | peut etre appeler une fonction dans la classe entity User? Bien regarder si sa existe déjà en spring
                    .pseudo(userDto.getPseudo())
                    .created_at(Date.from(Instant.now()))
                    .build();

            log.debug("user: {}", user);

            // return ResponseSuccessDto
            ResponseSuccessDto responseSuccessDto =
                    new ResponseSuccessDto().toBuilder()
                            .message("Ok")
                            //.data(user) //TODO: Creer l'objet plus haut et Renvoyer le UserCreatedResponseDto ici
                            .build();

            return ResponseEntity.ok(responseSuccessDto);

        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());
        }
        return null;
    }
}