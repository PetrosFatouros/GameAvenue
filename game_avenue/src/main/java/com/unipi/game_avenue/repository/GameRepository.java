package com.unipi.game_avenue.repository;

import com.unipi.game_avenue.model.GameModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameModel, Long> {
}
