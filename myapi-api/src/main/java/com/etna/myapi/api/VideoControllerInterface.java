package com.etna.myapi.api;

import com.etna.commondto.dto.EncodageDto;
import com.etna.myapi.constant.MyapiConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.etna.myapi.api.UserControllerInterface.USER_ID;

//@Service
@FeignClient(contextId = "VideoApi", name = MyapiConstant.NOM_SERVICE, url = "http://myapi:8080", path = "/myapi")
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
            @RequestParam(value = "name") Optional<String> name,
            @RequestParam(value = "page", defaultValue = "1") Optional<Integer> page,
            @RequestParam(value = "perPage", defaultValue = "5") Optional<Integer> perPage,
            @RequestParam(value = "user") Optional<String> sort,
            @RequestParam(value = "duration") Optional<Integer> duration
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
