package com.etna.myapi.controller.impl;

import com.etna.myapi.controller.VideoControllerInterface;
import com.etna.myapi.dataobjects.mappers.UserObjectMapper;
import com.etna.myapi.dataobjects.mappers.VideoObjectMapper;
import com.etna.myapi.dto.VideoResponseDto;
import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import com.etna.myapi.services.video.VideoServiceInterface;
import io.humble.video.Demuxer;
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

    @Autowired
    UserObjectMapper userObjectMapper;


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

            // Ouverture du fichier
            Demuxer demuxer = Demuxer.make();
            demuxer.open(file.getAbsolutePath(), null, false, true, null, null);

            // Durée en microsecondes
            long durationMicroSeconds = demuxer.getDuration();

            // Convertion en secondes
            double durationSeconds = durationMicroSeconds / 1_000_000.0;

            System.out.println("Durée de la vidéo : " + durationSeconds + " secondes");

            demuxer.close();

            // Convert Double to int
            log.debug("Durée de la vidéo : " + (int) durationSeconds + " secondes");

            video.duration((int) durationSeconds);

            // created_at
            video.created_at(Date.from(Instant.now()));

            // User by id
            if (userService.isUser(id)) {
                if (userRepository.findById(id).isPresent())
                    video.user(userRepository.findById(id).get());
                else {
                    // delete the file if it was created
                    if (file.exists()) {
                        if (file.delete())
                            log.info("Fichier supprimé");
                        else
                            log.error("Erreur lors de la suppression du fichier");
                    }
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            new HashMap<>(
                                    Map.of(
                                            "message", "User not found",
                                            "data", List.of()
                                    )
                            )
                    );
                }
            } else {
                // delete the file if it was created
                if (file.exists()) {
                    if (file.delete())
                        log.info("Fichier supprimé");
                    else
                        log.error("Erreur lors de la suppression du fichier");
                }

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

            // save the video in database
            videoRepository.save(video.build());

            log.info("Vidéo enregistrée en base de données");

            // convert the video to VideoResponseDto
            VideoResponseDto videoResponseDto = videoObjectMapper.toCreatedResponseDto(video.build());

            // return the video
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "OK",
                                    "data", videoResponseDto
                            )
                    )
            );


        } catch (InterruptedException e) {
            return ResponseEntity.status(500).body(
                    new HashMap<>(
                            Map.of(
                                    "message", "InterruptedException : " + e.getMessage(),
                                    "data", List.of()
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

            videos.get().peek(video -> log.debug("video : {}", video));

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

        } catch (NumberFormatException e) {
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
                Page<Video> videos = videoService.getAllVideo(page, perPage, name, duration, userOptional);

                // create the VideoResponseDto with mapper

                videos.get().peek(video -> log.debug("video : {}", video));

                log.debug("videos : {}", videos.get().collect(Collectors.toList()));

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
    public ResponseEntity<?> deleteVideo(Integer id) {
        try {
            log.info("Reception de la requête de suppression d'une vidéo");

            // get the video by id
            Optional<Video> video = videoRepository.findById(id);

            // if video not found return 404 Not Found
            if (video.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new HashMap<>(
                                Map.of(
                                        "message", "Not Found",
                                        "data", List.of()
                                )
                        )
                );

            // check if the video is the user's video
            if (!userService.isUser(video.get().getUser().getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new HashMap<>(
                                Map.of(
                                        "message", "Forbidden",
                                        "data", List.of()
                                )
                        )
                );

            // delete the file
            File file = new File("src/main/resources/" + video.get().getSource());
            if (file.exists()) {
                if (file.delete())
                    log.info("Fichier supprimé");
                else
                    log.error("Erreur lors de la suppression du fichier");
            }

            // delete the video in database
            videoRepository.delete(video.get());
            log.info("Vidéo supprimée en base de données");

            // return 200 OK
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

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
