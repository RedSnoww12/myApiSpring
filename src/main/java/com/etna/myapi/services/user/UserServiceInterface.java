package com.etna.myapi.services.user;

import com.etna.myapi.entity.User;
import org.springframework.data.domain.Page;

public interface UserServiceInterface {

    Page<User> getAllUser(int page, int size);

    Page<User> getAllUser(int page, int size, String pseudo);
}
