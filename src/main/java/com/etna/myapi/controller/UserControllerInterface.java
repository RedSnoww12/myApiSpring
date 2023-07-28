package com.etna.myapi.controller;

import com.etna.myapi.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Optional;

public interface UserControllerInterface {

    String USER = "/user";

    String USERS = "/users";
    String ROOT_INTERFACE = "/myapi";

    @PostMapping(path = USER)
    ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto);

    @GetMapping(path = USERS)
    ResponseEntity<?> allUsers(@RequestParam() Optional<String> pseudo, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int perPage);


}
