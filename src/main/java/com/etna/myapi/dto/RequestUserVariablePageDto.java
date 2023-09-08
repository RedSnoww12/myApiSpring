package com.etna.myapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RequestUserVariablePageDto {

    private String pseudo;
    private Integer page;
    private Integer perPage;
}
