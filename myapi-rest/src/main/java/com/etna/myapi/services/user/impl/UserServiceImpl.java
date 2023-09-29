package com.etna.myapi.services.user.impl;

import com.etna.myapi.entity.User;
import com.etna.myapi.services.repository.UserRepository;
import com.etna.myapi.services.user.UserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserServiceInterface {
    @Autowired
    UserRepository userRepository;

    @Override
    public Page<User> getAllUser(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> getAllUser(int page, int size, String pseudo) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByPseudoContainingIgnoreCase(pseudo, pageable);
    }

    @Override
    public Boolean isUser(Integer id) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> ouser = userRepository.findById(id);

        if (ouser.isEmpty()) {
            return false;
        }

        User user = ouser.get();

        String userUsername = user.getUsername();
        String userEmail = user.getEmail();

        return userUsername.equals(login)
                || userEmail.equals(login);
    }

    @Override
    public User getUserFromAuth() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(login);

        if (user == null) {
            user = userRepository.findByEmail(login);
        }

        return user;
    }

}
