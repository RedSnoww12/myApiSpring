package com.etna.myapi.services.repository;

import com.etna.myapi.entity.Video;
import org.springframework.data.repository.CrudRepository;

public interface VideoRepository extends CrudRepository<Video, Integer> {
}
