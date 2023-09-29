package com.etna.myapi.services.repository;

import com.etna.myapi.entity.Comment;
import com.etna.myapi.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Integer> {
    Page<Comment> findAllByVideo(Pageable page, Video video);
}
