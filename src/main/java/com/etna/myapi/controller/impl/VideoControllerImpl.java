package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.VideoControllerInterface;
import com.etna.myapi.dataobjects.mappers.VideoObjectMapper;
import com.etna.myapi.dto.VideoResponseDto;
import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import com.etna.myapi.services.video.VideoServiceInterface;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    VideoServiceInterface videoService;

    @Autowired
    VideoObjectMapper videoObjectMapper;


    @Override
    public ResponseEntity<?> createVideo(Integer id, String name, MultipartFile source) {
        try {
            log.info("Reception de la requête de création d'une vidéo");

            // get the extension file
            String extension = Objects.requireNonNull(source.getOriginalFilename()).substring(source.getOriginalFilename().lastIndexOf("."));

            log.debug("Extension du fichier : " + extension);

            // if extension file is not video return 400 Bad Request
            if (
                    !extension.equals(".mp4")
                    && !extension.equals(".webm")
                    && !extension.equals(".ogg")
                    && !extension.equals(".mkv")
                    && !extension.equals(".avi")
                    && !extension.equals(".mov")
                    && !extension.equals(".mp3")
            )
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new HashMap<>(
                                Map.of(
                                        "message", "Bad Request",
                                        "code", 10001,
                                        "data", List.of()
                                )
                        )
                );

            // If a file with the same name already exists, we delete it
            File fileToDelete = new File("src/main/resources/videos/" + name + extension);
            if (fileToDelete.exists()) {
                log.debug("Fichier existant, suppression");
                if (fileToDelete.delete())
                    log.info("Fichier supprimé");
                else
                    log.error("Erreur lors de la suppression du fichier");
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

            // Retrieve duration of the video in seconds
            Long duration = file.length() / 1000000;
            // Convert Long to int
            video.duration(duration.intValue());

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

    @Override
    public ResponseEntity<?> getVideos(int page, int perPage, Optional<String> name, Optional<String> user, Optional<Integer> duration) {
        try {
            log.info("Reception de la requête de récupération des vidéos");

            Optional<User> oUser = Optional.empty();

            // condition user if is an integer or a string
            if (user.isPresent()) {
                // if user is an integer
                if (userService.isUser(Integer.parseInt(user.get())) || !userService.isUser(Integer.parseInt(user.get()))) {
                    // get user by id
                    oUser = userRepository.findById(Integer.parseInt(user.get()));

                    // if user not found return 404 Not Found

                    if (oUser.isEmpty())
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                new HashMap<>(
                                        Map.of(
                                                "message", "Not Found",
                                                "data", List.of()
                                        )
                                )
                        );

                }
            }

            // if user found
            // get all videos by user
            Page<Video> videos = videoService.getAllVideo(page, perPage, name, duration, oUser);

            // create the VideoResponseDto with mapper

            Page<VideoResponseDto> reponseVideos = new PageImpl<>(videos.get()
                    .map(video -> videoObjectMapper.toCreatedResponseDto(video))
                    .collect(Collectors.toList()), videos.getPageable(), videos.getTotalElements());

            // return 200 OK with data : videos and pagination
            return ResponseEntity.status(HttpStatus.OK).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "OK",
                                    "data", reponseVideos
                            )
                    )
            );

        }
        catch (NumberFormatException e) {
            try {
                // test with string user can be pseudo
                Optional<User> userOptional = Optional.ofNullable(userRepository.findByPseudo(user.get()));

                // if user not found return 404 Not Found
                if (userOptional.isEmpty())
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            new HashMap<>(
                                    Map.of(
                                            "message", "Not Found",
                                            "data", List.of()
                                    )
                            )
                    );

                // get all videos by user
                Page<Video> videos = videoService.getAllVideo(page, perPage, name, duration,userOptional);

                // create the VideoResponseDto with mapper

                Page<VideoResponseDto> reponseVideos = new PageImpl<>(videos.get()
                        .map(video -> videoObjectMapper.toCreatedResponseDto(video))
                        .collect(Collectors.toList()), videos.getPageable(), videos.getTotalElements());

                // return 200 OK with data : videos and pagination
                return ResponseEntity.status(HttpStatus.OK).body(
                        new HashMap<>(
                                Map.of(
                                        "message", "OK",
                                        "data", reponseVideos
                                )
                        )
                );

            } catch (Exception ex) {
                return ResponseEntity.status(500).body(
                        new HashMap<>(
                                Map.of(
                                        "message", "Internal server error : " + ex.getMessage(),
                                        "data", List.of()
                                )
                        )
                );
            }
        }
        catch (Exception e) {
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
