package com.etna.myapi.dataobjects.mappers;

import com.etna.commondto.dto.CommentResponseDto;
import com.etna.myapi.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CommentObjectMapper {
    CommentResponseDto toCommentResponseDto(Comment comment);
}
