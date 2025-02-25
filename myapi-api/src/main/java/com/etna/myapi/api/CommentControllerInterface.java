package com.etna.myapi.api;

import com.etna.commondto.dto.CommentDto;
import com.etna.commondto.dto.RequestUserVariablePageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

import static com.etna.myapi.api.VideoControllerInterface.VIDEO_ID;

public interface CommentControllerInterface {

    String COMMENT = "/comment";
    String COMMENTS = "/comments";

    @PostMapping(VIDEO_ID + COMMENT)
    ResponseEntity<?> createComment(@PathVariable Integer id, @RequestBody CommentDto comment);

    @GetMapping(VIDEO_ID + COMMENTS)
    ResponseEntity<?> getCommentsOfVideo(@PathVariable Integer id,
                                         @RequestBody Optional<RequestUserVariablePageDto> requestUserVariablePageDto
    );
}
