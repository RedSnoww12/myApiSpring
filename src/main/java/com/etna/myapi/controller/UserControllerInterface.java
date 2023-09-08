package com.etna.myapi.controller;

import com.etna.myapi.dto.LoginDto;
import com.etna.myapi.dto.RequestUserVariablePageDto;
import com.etna.myapi.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

public interface UserControllerInterface {

    static String USER = "/user";
    static String USER_ID = "/user/{id}";
    String USERS = "/users";
    String AUTH = "/auth";


    String ROOT_INTERFACE = "/myapi";

    @PostMapping(path = USER)
    ResponseEntity<?> createUser(@RequestBody UserDto userDto);

    @GetMapping(path = USERS)
    ResponseEntity<?> AllUser(@RequestBody Optional<RequestUserVariablePageDto> requestUserVariablePageDto);

    @GetMapping(path = USER_ID)
    ResponseEntity<?> getUserById(@PathVariable Integer id);

    @DeleteMapping(path = USER_ID)
    ResponseEntity<?> deleteUser(@PathVariable Integer id);

    @PostMapping(path = AUTH)
    ResponseEntity<?> authenticate(@Valid @RequestBody LoginDto request);

    @PutMapping(path = USER_ID)
    ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto);

}
