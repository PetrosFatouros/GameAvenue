package com.unipi.game_avenue.service;

import com.unipi.game_avenue.model.GameModel;
import com.unipi.game_avenue.model.SearchGameModel;

import java.io.IOException;
import java.util.List;

public interface GameService {

    List<GameModel> callApi(String url) throws IOException;

    List<String> getAllTags();

    List<GameModel> searchGames(SearchGameModel search_data) throws IOException;

    void updateDatabase() throws IOException;

    GameModel getGame(Long game_id) throws IOException;

    void saveGameToList(Long game_id);

    List<GameModel> getUserGamesList();

    void deleteGameFromList(Long game_id);
}
