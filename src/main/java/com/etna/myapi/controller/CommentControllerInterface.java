package com.etna.myapi.controller;

import com.etna.myapi.dto.CommentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.etna.myapi.controller.VideoControllerInterface.VIDEO_ID;

public interface CommentControllerInterface {

    String COMMENT = "/comment";
    String COMMENTS = "/comments";

    @PostMapping(VIDEO_ID + COMMENT)
    ResponseEntity<?> createComment(@PathVariable Integer id, @RequestBody CommentDto comment);

    @GetMapping(VIDEO_ID + COMMENTS)
    ResponseEntity<?> getCommentsOfVideo(@PathVariable Integer id,
                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "perPage", defaultValue = "5") int perPage
    );
}
