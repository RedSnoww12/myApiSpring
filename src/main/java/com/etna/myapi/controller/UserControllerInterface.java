package com.etna.myapi.controller;

import com.etna.myapi.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface UserControllerInterface {

    String POST_USER = "/user";


    @PostMapping(path = POST_USER)
    ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto);
}
