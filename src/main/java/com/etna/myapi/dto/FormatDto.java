package com.etna.myapi.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FormatDto {
    @JsonProperty("1080")
    private String resolution1080;
    @JsonProperty("720")
    private String resolution720;
    @JsonProperty("480")
    private String resolution480;
    @JsonProperty("360")
    private String resolution360;
    @JsonProperty("240")
    private String resolution240;
    @JsonProperty("144")
    private String resolution144;
}
