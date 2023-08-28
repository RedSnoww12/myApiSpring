package com.etna.myapi.exceptions;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new HashMap<>(
                        Map.of("message", "Bad Request",
                                "code", 10001,
                                "data", List.of(ex.getName() + " doit être du type " + ex.getRequiredType().getName()))
                )
        );
    }

    // limite : 1048576 bytes = 1 Mo
    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<?> handleFileSizeLimitExceeded(FileSizeLimitExceededException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new HashMap<>(
                        Map.of("message", "Bad Request",
                                "code", 10002,
                                "data", List.of("La taille du fichier ne doit pas dépasser 1 Mo"))
                )
        );
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new HashMap<>(
                        Map.of("message", "Unauthorized")
                )
        );
    }

}