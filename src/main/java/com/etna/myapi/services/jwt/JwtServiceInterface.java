package com.etna.myapi.services.jwt;


import com.etna.myapi.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtServiceInterface {

    String SECRET_KEY = "wLfBwrmcLJVHBngcyGR3cXg5mY76hrQh";

    String extractUsername(String jwt);

    Claims extractAllClaims(String jwt);

    String generateToken(User user);

    Boolean isTokenValid(String token, User user);

}
