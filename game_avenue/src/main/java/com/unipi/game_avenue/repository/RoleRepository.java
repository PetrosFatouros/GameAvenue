package com.unipi.game_avenue.repository;

import com.unipi.game_avenue.model.RoleEnum;
import com.unipi.game_avenue.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleModel, Long> {
    Optional<RoleModel> findByName(RoleEnum role);
}
