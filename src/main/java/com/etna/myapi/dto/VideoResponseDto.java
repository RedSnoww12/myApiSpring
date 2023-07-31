package com.etna.myapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class VideoResponseDto {
    private String name;
    private Integer duration;
    private UserResponseDto user;
    private String source;
    private Date created_at;
    private Integer view;
    private Boolean enabled;
}
