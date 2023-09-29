package com.etna.myapi.controller;

import com.etna.commondto.dto.CommentDto;
import com.etna.commondto.dto.CommentResponseDto;
import com.etna.commondto.dto.PageDto;
import com.etna.commondto.dto.RequestUserVariablePageDto;
import com.etna.myapi.api.CommentControllerInterface;
import com.etna.myapi.dataobjects.ResponseEntityBuilder;
import com.etna.myapi.dataobjects.mappers.CommentObjectMapper;
import com.etna.myapi.entity.Comment;
import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.comment.CommentServiceInterface;
import com.etna.myapi.services.repository.CommentRepository;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import com.etna.myapi.services.video.VideoService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.etna.myapi.api.UserControllerInterface.ROOT_INTERFACE;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
@RequestMapping(ROOT_INTERFACE)
public class CommentController implements CommentControllerInterface {
    @Autowired
    VideoService videoService;
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserServiceInterface userServiceInterface;
    @Autowired
    CommentServiceInterface commentServiceInterface;
    @Autowired
    CommentObjectMapper commentObjectMapper;

    @Override
    public ResponseEntity<?> createComment(Integer id, CommentDto comment) {
        try {
            log.debug("video id : " + id);
            log.debug("comment : " + comment);

            // get the video by id
            Optional<Video> ovideo = videoRepository.findById(id);

            if (ovideo.isEmpty())
                return new ResponseEntityBuilder().buildNotFound();

            // check that the body is not null
            if (comment.getBody() == null)
                return new ResponseEntityBuilder().setData(List.of("body is null")).buildBadRequest(10001);

            // check that the body is not empty
            if (comment.getBody().isEmpty())
                return new ResponseEntityBuilder().setData(List.of("body is empty")).buildBadRequest(10001);

            // check that the body is not superior to varchar(255)
            if (comment.getBody().length() > 255)
                return new ResponseEntityBuilder().setData(List.of("body is too long")).buildBadRequest(10001);

            User user = userServiceInterface.getUserFromAuth();

            Comment newComment = Comment.builder()
                    .body(comment.getBody())
                    .video(ovideo.get())
                    .user(user)
                    .build();

            // save the comment
            Comment savedComment = commentRepository.save(newComment);

            CommentResponseDto commentResponseDto = commentObjectMapper.toCommentResponseDto(savedComment);

            return new ResponseEntityBuilder().setData(commentResponseDto).buildCreated();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(
                    Map.of("message", "Internal server error")
            ));
        }

    }

    @Override
    public ResponseEntity<?> getCommentsOfVideo(Integer id, Optional<RequestUserVariablePageDto> requestUserVariablePageDto) {
        // get the video by id
        Optional<Video> ovideo = videoRepository.findById(id);

        if (ovideo.isEmpty())
            return new ResponseEntityBuilder().buildNotFound();

        Video video = ovideo.get();

        int page = 1;
        int perPage = 5;

        if (requestUserVariablePageDto.isPresent()) {
            if (requestUserVariablePageDto.get().getPage() != null) page = requestUserVariablePageDto.get().getPage();
            if (requestUserVariablePageDto.get().getPerPage() != null)
                perPage = requestUserVariablePageDto.get().getPerPage();
        }

        // check page and perPage
        if (page < 1 || perPage < 1)
            return new ResponseEntityBuilder().setData(List.of("page or perPage is inferior to 1")).buildBadRequest(10001);

        if (page == 1)
            page = 0;
        else
            page = page - 1;

        // get the comments of the video
        //List<Comment> comments = video.getComments();
        Page<Comment> pcomments = commentServiceInterface.getAllCommentOfVideo(page, perPage, video);

        int totalPages = pcomments.getTotalPages();

        if (totalPages == 0)
            totalPages = 1;


        // page out of range
        if (page + 1 > totalPages)
            return new ResponseEntityBuilder().setData(List.of("page out of range")).buildBadRequest(10001);

        // Convert the comments to DTO
        List<CommentResponseDto> commentsDto = pcomments.stream()
                .sorted(
                        Comparator.comparing(Comment::getId)
                                .reversed()
                )
                .map(
                        comment ->
                                commentObjectMapper.toCommentResponseDto(comment)
                )
                .toList();

        PageDto pageDto = PageDto.builder()
                .current(page + 1)
                .total(totalPages)
                .build();

        return new ResponseEntityBuilder().setData(commentsDto).setPager(pageDto).buildOk();
    }
}
