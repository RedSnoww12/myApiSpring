package com.etna.myapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponseDto {
    private Integer id;
    private String name;
    private Integer duration;
    private UserResponseDto user;
    private String source;
    private Date created_at;
    private Integer view;
    private Boolean enabled;
    private FormatDto format;
}
