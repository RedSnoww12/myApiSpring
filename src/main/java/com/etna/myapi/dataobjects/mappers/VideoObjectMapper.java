package com.etna.myapi.dataobjects.mappers;

import com.etna.myapi.dto.VideoResponseDto;
import com.etna.myapi.entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface VideoObjectMapper {

    VideoResponseDto toCreatedResponseDto(Video video);
}
