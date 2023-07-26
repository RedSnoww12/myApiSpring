package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.UserControllerInterface;
import com.etna.myapi.dto.UserDto;
import com.etna.myapi.services.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            log.info("recupération d'un userDTO: {}", userDto);
        } catch (Exception e) {
            log.warn("Error : {}", e.getMessage());
        }
        return null;
    }
}