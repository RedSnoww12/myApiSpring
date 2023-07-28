package com.etna.myapi.services.repository;

import com.etna.myapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
    Page<User> findAll(Pageable pageable);

    Page<User> findByPseudoContainingIgnoreCase(String pseudo, Pageable pageable);

    User findByUsername(String username);
}