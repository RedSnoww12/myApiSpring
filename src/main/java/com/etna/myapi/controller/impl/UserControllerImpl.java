package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.UserControllerInterface;
import com.etna.myapi.dto.UserDto;
import com.etna.myapi.services.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Log4j2
@RequestMapping(path = "/demo")
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