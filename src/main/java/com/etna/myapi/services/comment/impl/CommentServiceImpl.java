package com.etna.myapi.services.comment.impl;

import com.etna.myapi.entity.Comment;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.comment.CommentServiceInterface;
import com.etna.myapi.services.repository.CommentRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CommentServiceImpl implements CommentServiceInterface {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Page<Comment> getAllCommentOfVideo(int page, int size, Video video) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<Comment> pcomment = commentRepository.findAllByVideo(pageable, video);

        log.debug("pcomment : " + pcomment);

        return commentRepository.findAllByVideo(pageable, video);
    }
}
