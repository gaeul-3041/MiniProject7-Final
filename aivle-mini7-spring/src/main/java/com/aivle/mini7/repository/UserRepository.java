package com.aivle.mini7.repository;

import com.aivle.mini7.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Override
    boolean existsById(String id);
}