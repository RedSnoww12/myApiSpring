package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.CommentControllerInterface;
import com.etna.myapi.dto.CommentDto;
import com.etna.myapi.entity.Comment;
import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.repository.CommentRepository;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import com.etna.myapi.services.video.VideoServiceInterface;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.etna.myapi.controller.UserControllerInterface.ROOT_INTERFACE;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
@RequestMapping(ROOT_INTERFACE)
public class CommentController implements CommentControllerInterface {
    @Autowired
    VideoServiceInterface videoServiceInterface;
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserServiceInterface userServiceInterface;

    @Override
    public ResponseEntity<?> createComment(Integer id, CommentDto comment) {
        try {
            log.debug("video id : " + id);
            log.debug("comment : " + comment);

            // get the video by id
            Optional<Video> ovideo = videoRepository.findById(id);

            if (ovideo.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(
                        Map.of("message", "Not found")
                ));

            // check that the body is not null
            if (comment.getBody() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(
                        Map.of("message", "Bad request",
                                "code", 10001,
                                "data", List.of("body is null")
                        )
                ));

            // check that the body is not empty
            if (comment.getBody().isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(
                        Map.of("message", "Bad request",
                                "code", 10001,
                                "data", List.of("body is empty")
                        )
                ));

            // check that the body is not superior to varchar(255)
            if (comment.getBody().length() > 255)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(
                        Map.of("message", "Bad request",
                                "code", 10003,
                                "data", List.of("body is too long")
                        )
                ));

            User user = userServiceInterface.getUserFromAuth();

            Comment newComment = Comment.builder()
                    .body(comment.getBody())
                    .video(ovideo.get())
                    .user(user)
                    .build();

            // save the comment
            commentRepository.save(newComment);

            return ResponseEntity.status(HttpStatus.CREATED).body(new HashMap<>(
                    Map.of("message", "OK",
                            "data", comment
                    )
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(
                    Map.of("message", "Internal server error")
            ));
        }

    }

    @Override
    public ResponseEntity<?> getCommentsOfVideo(Integer id) {
        // get the video by id
        Optional<Video> ovideo = videoRepository.findById(id);

        if (ovideo.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(
                    Map.of("message", "Not found")
            ));

        Video video = ovideo.get();

        // get the comments of the video
        List<Comment> comments = video.getComments();

        // Convert the comments to DTO
        List<CommentDto> commentsDto = comments.stream().map(
                comment -> CommentDto.builder()
                        .body(comment.getBody())
                        .build()
        ).toList();


        return ResponseEntity.status(HttpStatus.OK).body(new HashMap<>(
                Map.of("message", "OK",
                        "data", commentsDto
                )
        ));
    }
}
