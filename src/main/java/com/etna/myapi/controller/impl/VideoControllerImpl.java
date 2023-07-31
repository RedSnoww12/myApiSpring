package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.VideoControllerInterface;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.etna.myapi.controller.UserControllerInterface.ROOT_INTERFACE;

@RestController
@AllArgsConstructor
@Log4j2
@RequestMapping(path = ROOT_INTERFACE)
public class VideoControllerImpl implements VideoControllerInterface {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    UserServiceInterface userService;


    @Override
    public ResponseEntity<?> createVideo(Integer id, String name, MultipartFile source) {
        try {
            log.info("Reception de la requête de création d'une vidéo");

            // get the extension file
            String extension = Objects.requireNonNull(source.getOriginalFilename()).substring(source.getOriginalFilename().lastIndexOf("."));

            log.debug("Extension du fichier : " + extension);

            // If a file with the same name already exists, we delete it
            File fileToDelete = new File("src/main/resources/videos/" + name + extension);
            if (fileToDelete.exists()) {
                log.debug("Fichier existant, suppression");
                fileToDelete.delete();
            }

            // store the file in resources/videos
            File file = new File("src/main/resources/videos/" + name + extension);
            Files.copy(source.getInputStream(), file.toPath());

            log.debug("Fichier enregistré");

            // create the video Entity
            Video.VideoBuilder video = Video.builder();

            // name
            video.name(name);

            // source
            video.source("videos/" + name + extension);

            // Retrieve duration of the video
            // TODO : get duration of the video
            video.duration(0);

            // created_at
            video.created_at(Date.from(Instant.now()));

            // User by id
            if (userService.isUser(id)) {
                if (userRepository.findById(id).isPresent())
                    video.user(userRepository.findById(id).get());
                else
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            new HashMap<>(
                                    Map.of(
                                            "message", "User not found",
                                            "data", List.of()
                                    )
                            )
                    );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new HashMap<>(
                                Map.of(
                                        "message", "User not found",
                                        "data", List.of()
                                )
                        )
                );
            }

            // view
            video.view(0);

            // enabled
            video.enabled(true);


            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "OK",
                                    "data", video.build()
                            )
                    )
            );


        } catch (IOException e) {
            return ResponseEntity.status(500).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "IOException : " + e.getMessage(),
                                    "data", List.of()
                            )
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "Internal server error : " + e.getMessage(),
                                    "data", List.of()
                            )
                    )
            );
        }
    }
}
