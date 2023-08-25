package com.etna.myapi.services.video.impl;

import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.video.VideoServiceInterface;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class VideoServiceImpl implements VideoServiceInterface {

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public Page<Video> getAllVideo(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return videoRepository.findAll(pageable);
    }

    @Override
    public Page<Video> getAllVideo(int page, int size, Optional<String> videoName, Optional<Integer> duration, Optional<User> user) {

        if (page < 1) page = 1;

        if (size < 1) size = 1;

        Pageable pageable = Pageable.ofSize(size).withPage(page - 1);
        Page<Video> pvideos = videoRepository.findAll(pageable);

        log.debug("video from getAllVideo: {}", pvideos.get().collect(Collectors.toList()));

        pvideos.get().peek(video -> log.info(video.getName()));

        // filter videos by videoName if not null, duration if not 0, user if not null
        List<Video> videos = pvideos.get().filter(
            video -> videoName.isEmpty() || video.getName().equals(videoName.get())
        ).filter(
            video -> duration.isEmpty() || video.getDuration() >= duration.get()
        ).filter(
            video -> user.isEmpty() || video.getUser().equals(user.get())
        ).toList();

        // Build a new Page<Video> from the filtered videos
        return new PageImpl<>(videos, pageable, videos.size());

    }

}
