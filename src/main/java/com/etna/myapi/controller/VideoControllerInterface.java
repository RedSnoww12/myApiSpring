package com.etna.myapi.controller;

import com.etna.commondto.dto.EncodageDto;
import com.etna.commondto.dto.VideoGetRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            @RequestBody Optional<VideoGetRequestDto> videoGetRequestDto
    );

    @DeleteMapping(path = VIDEO_ID)
    ResponseEntity<?> deleteVideo(@PathVariable Integer id);

    @PutMapping(path = VIDEO_ID)
    ResponseEntity<?> updateVideo(@PathVariable Integer id,
                                  @RequestParam("name") Optional<String> name, //TODO: change to RequestBody
                                  @RequestParam("user") Optional<Integer> UserId
    );

    @GetMapping(path = USER_ID + VIDEOS)
    ResponseEntity<?> getVideosByUser(@PathVariable Integer id,
                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "perPage", defaultValue = "5") int perPage
    );

    @PatchMapping(path = VIDEO_ID)
    ResponseEntity<?> encodeVideo(@PathVariable Integer id, @RequestBody EncodageDto encodageDto);
}
