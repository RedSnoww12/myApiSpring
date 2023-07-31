package com.etna.myapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

}
