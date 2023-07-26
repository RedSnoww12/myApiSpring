package com.etna.myapi.dto;

import com.etna.myapi.User;
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
    private User data;


}
