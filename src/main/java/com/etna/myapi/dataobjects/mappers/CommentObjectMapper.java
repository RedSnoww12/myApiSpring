package com.etna.myapi.dataobjects.mappers;

import com.etna.myapi.dto.CommentResponseDto;
import com.etna.myapi.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CommentObjectMapper {
    CommentResponseDto toCommentResponseDto(Comment comment);
}
