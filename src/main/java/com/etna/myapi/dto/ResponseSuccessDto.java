package com.etna.myapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ResponseSuccessDto {

    private String message;
    private UserCreatedResponseDto data;

    // TODO: creer la classe
    /* UserCreatedResponseDto
     * {
     *  username;
     *  email;
     *  pseudo;
     * */

}
