package com.etna.myapi.services.video;

import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface VideoServiceInterface {
    Page<Video> getAllVideo(int page, int size);

    Page<Video> getAllVideo(int page, int size, Optional<String> videoName, Optional<Integer> duration, Optional<User> user);




}
