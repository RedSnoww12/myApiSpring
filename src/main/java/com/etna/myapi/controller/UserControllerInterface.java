package com.etna.myapi.controller;

import com.etna.myapi.config.AuthenticationRequest;
import com.etna.myapi.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

public interface UserControllerInterface {

    String USER = "/user";
    String USER_ID = "/user/{id}";
    String USERS = "/users";
    String AUTH = "/auth";


    String ROOT_INTERFACE = "/myapi";

    @PostMapping(path = USER)
    ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto);

    @GetMapping(path = USERS)
    ResponseEntity<?> allUsers(@RequestParam() Optional<String> pseudo, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int perPage);

    @DeleteMapping(path = USER_ID)
    ResponseEntity<?> deleteUser(@PathVariable Long id);

    @PostMapping(path = AUTH)
    ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest request);

}
