package com.unipi.game_avenue.repository;

import com.unipi.game_avenue.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, String> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
