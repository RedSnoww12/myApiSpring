package com.etna.myapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.etna.myapi.controller.UserControllerInterface.USER_ID;

public interface VideoControllerInterface {

    String VIDEO = "/video";
    String VIDEO_ID = "/video/{id}";
    String VIDEOS = "/videos";
    String ROOT_INTERFACE = "/myapi";

    //MultipartFile

    @PostMapping(path = USER_ID + VIDEO)
        // get from form-data name: String, source: MultipartFile
    ResponseEntity<?> createVideo(@PathVariable Integer id, @RequestParam("name") String name, @RequestParam("source") MultipartFile source);

    @GetMapping(path = VIDEOS)
    ResponseEntity<?> getVideos(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "perPage", defaultValue = "5") int perPage,
            @RequestParam(value = "name") Optional<String> name,
            // User can be a String or an Integer
@           RequestParam(value = "user") Optional<String> user,
            @RequestParam(value = "duration") Optional<Integer> duration
    );
}
