package com.etna.myapi.exceptions;

import com.etna.myapi.dataobjects.ResponseEntityBuilder;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntityBuilder().setData(List.of("Le type de l'argument est invalide")).buildBadRequest(10001);
    }

    // limite : 1048576 bytes = 1 Mo
    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<?> handleFileSizeLimitExceeded(FileSizeLimitExceededException ex) {
        return new ResponseEntityBuilder().setData(List.of("Le fichier est trop volumineux")).buildBadRequest(10001);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        return new ResponseEntityBuilder().buildUnauthorized();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        return new ResponseEntityBuilder().setData(List.of("Le format de la requête est invalide")).buildBadRequest(10001);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        return new ResponseEntityBuilder().setData(List.of("Le fichier est manquant")).buildBadRequest(10001);
    }

}