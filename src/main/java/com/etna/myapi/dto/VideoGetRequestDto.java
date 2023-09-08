package com.etna.myapi.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class VideoGetRequestDto {
    /*
    * {
	"name": string,
	"user": string|int,
	"duration": int,
	"page": int,
	"perPage": int
    }*/
    @Nullable
    private String name;
    @Nullable
    private String user;
    @Nullable
    private Integer duration;
    @Nullable
    private Integer page;
    @Nullable
    private Integer perPage;
}
