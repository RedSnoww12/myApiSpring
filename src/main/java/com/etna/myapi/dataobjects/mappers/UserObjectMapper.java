package com.etna.myapi.dataobjects.mappers;

import com.etna.myapi.dto.UserCreatedResponseDto;
import com.etna.myapi.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface UserObjectMapper {

    UserCreatedResponseDto toCreatedResponseDto(User user);

}
