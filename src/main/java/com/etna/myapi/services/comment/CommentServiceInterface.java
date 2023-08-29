package com.etna.myapi.services.comment;

import com.etna.myapi.entity.Comment;
import com.etna.myapi.entity.Video;
import org.springframework.data.domain.Page;

public interface CommentServiceInterface {
    Page<Comment> getAllCommentOfVideo(int page, int size, Video video);
}
