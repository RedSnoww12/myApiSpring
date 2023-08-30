package com.etna.myapi.dataobjects;

import com.etna.myapi.dto.PageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntityBuilder {
    public static final String BAD_REQUEST = "Bad Request";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
    public static final String NOT_FOUND = "Not Found";
    public static final String OK = "OK";
    private String message;
    private HttpStatus status;
    private Integer code;
    private Object data;


    private PageDto pager;

    public ResponseEntityBuilder() {
    }

    private ResponseEntityBuilder(HttpStatus status) {
        this.status = status;
    }

    public ResponseEntityBuilder setPager(PageDto pager) {
        this.pager = pager;
        return this;
    }

    private ResponseEntityBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public ResponseEntityBuilder setData(Object data) {
        this.data = data;
        return this;
    }

    private ResponseEntityBuilder setCode(Integer code) {
        this.code = code;
        return this;
    }


    private ResponseEntity<?> build() {
        if (pager != null) {
            return ResponseEntity.status(this.status).body(new HashMap<>(
                    Map.of(
                            "message", this.message,
                            "data", this.data,
                            "pager", this.pager
                    )
            ));
        }
        if (data == null)
            return ResponseEntity.status(this.status).body(new HashMap<>(
                    Map.of(
                            "message", this.message
                    )
            ));

        if (this.code != null)
            return ResponseEntity.status(this.status).body(new HashMap<>(
                    Map.of(
                            "message", this.message,
                            "code", this.code,
                            "data", this.data
                    )
            ));

        return ResponseEntity.status(this.status).body(new HashMap<>(
                Map.of(
                        "message", this.message,
                        "data", this.data
                )
        ));
    }


    private ResponseEntity<?> buildNoContent() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<?> buildOk() {
        this.status = HttpStatus.OK;
        this.message = OK;
        return build();
    }

    public ResponseEntity<?> buildCreated() {
        this.status = HttpStatus.CREATED;
        this.message = OK;
        return build();
    }

    public ResponseEntity<?> buildBadRequest(Integer code) {
        this.status = HttpStatus.BAD_REQUEST;
        this.message = BAD_REQUEST;
        this.code = code;
        return build();
    }

    public ResponseEntity<?> buildUnauthorized() {
        this.status = HttpStatus.UNAUTHORIZED;
        this.message = UNAUTHORIZED;
        return build();
    }

    public ResponseEntity<?> buildForbidden() {
        this.status = HttpStatus.FORBIDDEN;
        this.message = FORBIDDEN;
        return build();
    }

    public ResponseEntity<?> buildNotFound() {
        this.status = HttpStatus.NOT_FOUND;
        this.message = NOT_FOUND;
        return build();
    }

    public ResponseEntity<?> buildDeleted() {
        return buildNoContent();
    }


}
