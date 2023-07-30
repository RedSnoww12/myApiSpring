package com.etna.myapi.services.jwt;


import io.jsonwebtoken.Claims;

public interface JwtServiceInterface {

    String SECRET_KEY = "f096f1d8756c1fbac01994a5096836c8c9805fb70b05b0086a6b2d867ce82eda";

    String extractUsername(String jwt);

    Claims extractAllClaims(String jwt);

}
