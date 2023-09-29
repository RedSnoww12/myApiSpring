package com.etna.myapi.controller;

import com.etna.commondto.dto.*;
import com.etna.myapi.api.VideoControllerInterface;
import com.etna.myapi.dataobjects.ResponseEntityBuilder;
import com.etna.myapi.dataobjects.mappers.UserObjectMapper;
import com.etna.myapi.dataobjects.mappers.VideoObjectMapper;
import com.etna.myapi.entity.User;
import com.etna.myapi.entity.Video;
import com.etna.myapi.entity.VideoFormat;
import com.etna.myapi.publisher.RabbitMQProducer;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.repository.VideoRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import com.etna.myapi.services.video.VideoService;
import io.humble.video.Demuxer;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import static com.etna.myapi.api.UserControllerInterface.ROOT_INTERFACE;

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
    VideoService videoService;
    @Autowired
    VideoObjectMapper videoObjectMapper;
    @Autowired
    UserObjectMapper userObjectMapper;
    @Autowired
    RabbitMQProducer rabbitMQProducer;


    @Override
    public ResponseEntity<?> createVideo(Integer id, String name, MultipartFile source) {
        try {
            log.info("Reception de la requête de création d'une vidéo");

            // check if the user exist
            Optional<User> ouser = userRepository.findById(id);

            if (ouser.isEmpty())
                return new ResponseEntityBuilder().buildNotFound();

            // check if the user is the owner of the video
            if (!userService.isUser(id))
                return new ResponseEntityBuilder().buildForbidden();

            User user = ouser.get();

            // check if file is empty
            if (source.isEmpty())
                return new ResponseEntityBuilder().setData(List.of("Le fichier est vide")).buildBadRequest(10001);

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
                return new ResponseEntityBuilder()
                        .setData(List.of())
                        .buildBadRequest(10001);

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

            // user
            video.user(user);

            // name
            video.name(name);

            // source
            video.source("videos/" + name + extension);

            log.debug("before demuxer");
            log.debug("Chemin du fichier : " + file.getAbsolutePath());

            // Ouverture du fichier
            Demuxer demuxer = Demuxer.make();
            demuxer.open(file.getAbsolutePath(), null, false, true, null, null);

            // Durée en microsecondes
            long durationMicroSeconds = demuxer.getDuration();

            // Convertion en secondes
            Double durationSeconds = durationMicroSeconds / 1_000_000.0;

            if (durationSeconds <= 1.00)
                durationSeconds = 1.00;

            demuxer.close();

            // Convert Double to int
            log.debug("Durée de la vidéo : " + durationSeconds + " secondes");

            video.duration(durationSeconds.intValue());

            // created_at
            video.created_at(Date.from(Instant.now()));

            // view
            video.view(0);

            // enabled
            video.enabled(true);

            // save the video in database
            Video savedVideo = videoRepository.save(video.build());

            log.info("Vidéo enregistrée en base de données");

            // convert the video to VideoResponseDto
            VideoResponseDto videoResponseDto = videoObjectMapper.toCreatedResponseDto(savedVideo);

            // send the video to rabbitmq
            RabbitMQVideoDto rabbitMQVideoDto = RabbitMQVideoDto.builder()
                    .video(videoResponseDto)
                    .path(file.getAbsolutePath())
                    .build();

            rabbitMQProducer.send(rabbitMQVideoDto);

            // return the video
            return new ResponseEntityBuilder()
                    .setData(videoResponseDto)
                    .buildCreated();


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
    public ResponseEntity<?> getVideos(
            Optional<String> name,
            Optional<Integer> page,
            Optional<Integer> perPage,
            Optional<String> user,
            Optional<Integer> duration
    ) {
        try {
            log.info("Reception de la requête de récupération des vidéos");

            // build the VideoGetRequestDto with parameters
            Optional<VideoGetRequestDto> videoGetRequestDto = Optional.ofNullable(
                    VideoGetRequestDto.builder()
                            .name(name.orElse(null))
                            .page(page.orElse(null))
                            .perPage(perPage.orElse(null))
                            .user(user.orElse(null))
                            .duration(duration.orElse(null))
                            .build()
            );

            int dpage = 1;
            int dperPage = 5;

            Optional<User> oUser = Optional.empty();
            VideoGetRequestDto videoGetRequestDto1;

            if (videoGetRequestDto.isPresent()) {
                videoGetRequestDto1 = videoGetRequestDto.get();
                // condition user if is an integer or a string
                if (videoGetRequestDto1.getUser() != null) {
                    // if user is an integer
                    if (userService.isUser(Integer.parseInt(videoGetRequestDto1.getUser())) || !userService.isUser(Integer.parseInt(videoGetRequestDto1.getUser()))) {
                        // get user by id
                        oUser = userRepository.findById(Integer.parseInt(videoGetRequestDto1.getUser()));

                        // if user not found return 404 Not Found

                        if (oUser.isEmpty())
                            return new ResponseEntityBuilder().buildNotFound();

                    }
                }
                if (videoGetRequestDto1.getPage() != null) dpage = videoGetRequestDto1.getPage();
                if (videoGetRequestDto1.getPerPage() != null) dperPage = videoGetRequestDto1.getPerPage();


                // if user found
                // get all videos by user
                Page<Video> videos = videoService.getAllVideo(dpage, dperPage, Optional.ofNullable(videoGetRequestDto1.getName()), Optional.ofNullable(videoGetRequestDto1.getDuration()), oUser);

                List<VideoResponseDto> videoResponseDto = videos.get()
                        .sorted(Comparator.comparing(Video::getId).reversed())
                        .map(video -> {
                            VideoResponseDto mvideoResponseDto = videoObjectMapper.toCreatedResponseDto(video);
                            mvideoResponseDto.setFormat(videoFormatToFormatDto(video.getVideoFormats()));
                            return mvideoResponseDto;
                        })
                        .toList();

                PageDto pager = new PageDto().toBuilder()
                        .current(videos.getNumber() + 1)
                        .total(videos.getTotalPages())
                        .build();

                return new ResponseEntityBuilder()
                        .setData(videoResponseDto)
                        .setPager(pager)
                        .buildOk();
            } else {
                Page<Video> videos = videoService.getAllVideo(dpage, dperPage, Optional.empty(), Optional.empty(), Optional.empty());

                List<VideoResponseDto> videoResponseDto = videos.get()
                        .sorted(Comparator.comparing(Video::getId).reversed())
                        .map(video -> {
                            VideoResponseDto mvideoResponseDto = videoObjectMapper.toCreatedResponseDto(video);
                            mvideoResponseDto.setFormat(videoFormatToFormatDto(video.getVideoFormats()));
                            return mvideoResponseDto;
                        })
                        .toList();

                PageDto pager = new PageDto().toBuilder()
                        .current(videos.getNumber() + 1)
                        .total(videos.getTotalPages())
                        .build();

                return new ResponseEntityBuilder()
                        .setData(videoResponseDto)
                        .setPager(pager)
                        .buildOk();
            }
        } catch (NumberFormatException e) {
            try {
                // test with string user can be pseudo
                Optional<User> userOptional = Optional.ofNullable(userRepository.findByPseudo(user.get()));

                // if user not found return 404 Not Found
                if (userOptional.isEmpty())
                    return new ResponseEntityBuilder().buildNotFound();

                int dpage = 1;
                int dperPage = 5;

                if (page.isPresent()) dpage = page.get();
                if (perPage.isPresent()) dperPage = perPage.get();


                // get all videos by user
                Page<Video> videos = videoService.getAllVideo(dpage, dperPage, Optional.ofNullable(name.get()), Optional.ofNullable(duration.get()), userOptional);

                // create the VideoResponseDto with mapper

                videos.get().peek(video -> log.debug("video : {}", video));

                log.debug("videos : {}", videos.get().collect(Collectors.toList()));

                List<VideoResponseDto> videoResponseDto = videos.get().map(video -> videoObjectMapper.toCreatedResponseDto(video)).toList();

                PageDto pager = new PageDto().toBuilder()
                        .current(videos.getNumber() + 1)
                        .total(videos.getTotalPages())
                        .build();


                return new ResponseEntityBuilder()
                        .setData(videoResponseDto)
                        .setPager(pager)
                        .buildOk();

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
                return new ResponseEntityBuilder().buildNotFound();

            // check if the video is the user's video
            if (!userService.isUser(video.get().getUser().getId()))
                return new ResponseEntityBuilder().buildForbidden();

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
            return new ResponseEntityBuilder().buildDeleted();

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
    public ResponseEntity<?> updateVideo(Integer id, Optional<String> name, Optional<Integer> UserId) {
        try {
            log.info("Reception de la requête de modification d'une vidéo");

            // get the video by id
            Optional<Video> video = videoRepository.findById(id);

            // if video not found return 404 Not Found
            if (video.isEmpty())
                return new ResponseEntityBuilder().buildNotFound();

            // check if the video is the user's video
            if (!userService.isUser(video.get().getUser().getId()))
                return new ResponseEntityBuilder().buildForbidden();

            // if the userId doesn't exist return 404 Not Found
            if (UserId.isPresent() && userRepository.findById(UserId.get()).isEmpty())
                return new ResponseEntityBuilder().buildNotFound();

            // update the video
            if (name.isPresent()) {
                // edit the filename in resources/videos
                File file = new File("src/main/resources/" + video.get().getSource());
                String extension = Objects.requireNonNull(file.getName()).substring(file.getName().lastIndexOf("."));
                File newFile = new File("src/main/resources/videos/" + name.get() + extension);
                if (file.renameTo(newFile))
                    log.info("Fichier renommé");
                else
                    log.error("Erreur lors du renommage du fichier");

                // update the source in database
                video.get().setName(name.get());
                // update the source in database
                video.get().setSource("videos/" + name.get() + extension);
            }


            // update the user in database
            if (UserId.isPresent()) {
                // get the user by id
                Optional<User> user = userRepository.findById(UserId.get());

                log.debug("id : {}", UserId.get());

                log.debug("user : {}", user.get());

                // if user not found return 404 Not Found
                if (user.isEmpty())
                    return new ResponseEntityBuilder().buildNotFound();

                video.get().setUser(user.get());
            }

            // save the video in database
            videoRepository.save(video.get());

            // convert the video to VideoResponseDto
            VideoResponseDto videoResponseDto = videoObjectMapper.toCreatedResponseDto(video.get());

            // set FormatDto
            videoResponseDto.setFormat(videoFormatToFormatDto(video.get().getVideoFormats()));

            // return 200 OK with data : video
            return new ResponseEntityBuilder()
                    .setData(videoResponseDto)
                    .buildOk();

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
    public ResponseEntity<?> getVideosByUser(Integer id, int page, int perPage) {
        try {
            log.info("Reception de la requête de récupération des vidéos d'un utilisateur");

            // get the user by id
            Optional<User> user = userRepository.findById(id);

            // if user not found return 404 Not Found
            if (user.isEmpty())
                return new ResponseEntityBuilder().buildNotFound();

            // get all videos by user
            Page<Video> videos = videoService.getAllVideosByUser(page, perPage, user.get());

            if (videos.isEmpty())
                log.debug("videos is empty");

            // create the VideosPageResponseDto
            List<VideoResponseDto> responseVideos = videos.get()
                    .sorted(Comparator.comparing(Video::getId).reversed())
                    .map(
                            video -> {
                                VideoResponseDto videoResponseDto = videoObjectMapper.toCreatedResponseDto(video);
                                videoResponseDto.setFormat(videoFormatToFormatDto(video.getVideoFormats()));
                                return videoResponseDto;
                            }
                    )
                    .toList();

            PageDto pager = new PageDto().toBuilder()
                    .current(videos.getNumber() + 1)
                    .total(videos.getTotalPages())
                    .build();

            // return 200 OK with data : videos and pagination
            return new ResponseEntityBuilder().setData(responseVideos).setPager(pager).buildOk();

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
    public ResponseEntity<?> encodeVideo(Integer id, EncodageDto encodageDto) {
        try {
            // check if the video exist
            Optional<Video> ovideo = videoRepository.findById(id);

            if (ovideo.isEmpty())
                return new ResponseEntityBuilder().buildNotFound();

            Video video = ovideo.get();

            if (encodageDto == null)
                return new ResponseEntityBuilder().setData(List.of("Le format est vide")).buildBadRequest(10001);

            // check encodageDto
            if (encodageDto.getFormat() == null || encodageDto.getFormat().isEmpty())
                return new ResponseEntityBuilder().setData(List.of("Le format est vide")).buildBadRequest(10001);

            // check if encodage.file
            if (encodageDto.getFile() == null || encodageDto.getFile().isEmpty())
                return new ResponseEntityBuilder().setData(List.of("Le fichier est vide")).buildBadRequest(10001);

            VideoFormat videoFormat = VideoFormat.builder()
                    .code(encodageDto.getFormat())
                    .uri(encodageDto.getFile())
                    .video(video)
                    .build();

            video.getVideoFormats().add(videoFormat);

            video = videoRepository.save(video);

            VideoResponseDto videoResponseDto = videoObjectMapper.toCreatedResponseDto(video);

            FormatDto formatDto = videoFormatToFormatDto(video.getVideoFormats());

            videoResponseDto.setFormat(formatDto);

            return new ResponseEntityBuilder().setData(videoResponseDto).buildOk();

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

    public static FormatDto videoFormatToFormatDto(List<VideoFormat> videoFormats) {
        FormatDto formatDto = new FormatDto();

        for (VideoFormat videoFormat : videoFormats) {
            switch (videoFormat.getCode()) {
                case "1080" -> formatDto.setResolution1080(!videoFormat.getUri().isEmpty() ? videoFormat.getUri() : "");
                case "720" -> formatDto.setResolution720(!videoFormat.getUri().isEmpty() ? videoFormat.getUri() : "");
                case "480" -> formatDto.setResolution480(!videoFormat.getUri().isEmpty() ? videoFormat.getUri() : "");
                case "360" -> formatDto.setResolution360(!videoFormat.getUri().isEmpty() ? videoFormat.getUri() : "");
                case "240" -> formatDto.setResolution240(!videoFormat.getUri().isEmpty() ? videoFormat.getUri() : "");
                case "144" -> formatDto.setResolution144(!videoFormat.getUri().isEmpty() ? videoFormat.getUri() : "");
                default -> formatDto = null;
            }
        }

        return formatDto;
    }


}
