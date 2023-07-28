package com.etna.myapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UsersPageResponseDto {

    String message;
    List<UserCreatedResponseDto> data;
    PageDto pager;
}
