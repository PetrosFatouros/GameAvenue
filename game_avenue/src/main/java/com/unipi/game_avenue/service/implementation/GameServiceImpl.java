package com.unipi.game_avenue.service.implementation;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.unipi.game_avenue.model.GameModel;
import com.unipi.game_avenue.model.SearchGameModel;
import com.unipi.game_avenue.model.UserModel;
import com.unipi.game_avenue.repository.GameRepository;
import com.unipi.game_avenue.repository.UserRepository;
import com.unipi.game_avenue.service.GameService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Override
    public List<GameModel> callApi(String url_api) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Request
        Request request = new Request.Builder()
                .url(url_api)
                .get()
                .addHeader("X-RapidAPI-Host", "free-to-play-games-database.p.rapidapi.com")
                .addHeader("X-RapidAPI-Key", "85d7bede2fmsh905c6165a0954bbp1b65e5jsn2e7b36cdd213")
                .build();

        // Get Response
        Response response = client.newCall(request).execute();

        // Gather games in a list
        List<GameModel> games = new ArrayList<>();

        String responseBody = response.body().string();

        // The games from the response are in JSON format
        JSONArray json_games;

        try{
            json_games = new JSONArray(responseBody);
        }
        catch (JSONException e){
            json_games = new JSONArray();
        }

        // Convert the JSON formatted games into GameModel objects
        for (int i = 0; i < json_games.length(); i++) {

            JSONObject game = json_games.getJSONObject(i);
            games.add(new GameModel(
                    game.getLong("id"),
                    game.getString("title"),
                    game.getString("thumbnail"),
                    game.getString("short_description"),
                    game.getString("game_url"),
                    game.getString("genre"),
                    game.getString("platform"),
                    game.getString("publisher"),
                    game.getString("developer"),
                    game.getString("release_date")
            ));
        }

        return games;
    }

    // Return all API available tags/genres/categories
    @Override
    public List<String> getAllTags() {
        return Arrays.asList(
                "all", "mmorpg", "shooter", "strategy", "moba", "racing", "sports", "social", "sandbox",
                "open-world", "survival", "pvp", "pve", "pixel", "voxel", "zombie", "turn-based",
                "first-person", "third-Person", "top-down", "tank", "space", "sailing", "side-scroller",
                "superhero", "permadeath", "card", "battle-royale", "mmo", "mmofps", "mmotps", "3d",
                "2d", "anime", "fantasy", "sci-fi", "fighting", "action-rpg", "action", "military",
                "martial-arts", "flight", "low-spec", "tower-defense", "horror", "mmorts");
    }

    // Search for games
    @Override
    public List<GameModel> searchGames(SearchGameModel game) throws IOException {

        // Filter requested parameters and call API
        List<GameModel> games_results;

        StringBuilder params = new StringBuilder();

        if(game.getBasic_search()) {

            boolean and = false; // add '&' at the end of a parameter
            boolean questing_mark = true; // add '?' at the start of the parameters

            if (game.getPlatform() != null) {
                params.append("?").append("platform=").append(game.getPlatform());
                and = true;
                questing_mark = false;
            }

            if (game.getGenre() != null && !Objects.equals(game.getGenre(), "all")) {
                if (questing_mark) {
                    params.append("?");
                    questing_mark = false;
                }
                if (and) {
                    params.append("&");
                } else {
                    and = true;
                }
                params.append("category=").append(game.getGenre());
            }

            if (game.getSort_by() != null) {
                if (questing_mark) {
                    params.append("?");
                }
                if (and) {
                    params.append("&");
                }
                params.append("sort-by=").append(game.getSort_by());
            }

            // Games are now filtered based on platform, genre and are sorted
            games_results = callApi(
                    "https://free-to-play-games-database.p.rapidapi.com/api/games" + params);
        }
        else{

            if(game.getGenre() != null){

                params.append("?");
                params.append("tag=");

                params.append(game.getGenre().replace(',', '.'));

                if (game.getPlatform() != null) {
                    params.append("&").append("platform=").append(game.getPlatform());
                }
            }

            // Games are now filtered based on tags and platform
            games_results = callApi(
                    "https://free-to-play-games-database.p.rapidapi.com/api/filter" + params);
        }

        // Filter games based on title
        if (game.getTitle() != null && game.getTitle().trim().length() > 0) {

            List<GameModel> filtered_games = new ArrayList<>();
            for (GameModel gameModel : games_results) {

                String search_title = game.getTitle().toLowerCase().trim();
                String game_title = gameModel.getTitle().toLowerCase().trim();

                if (game_title.contains(search_title)) {
                    filtered_games.add(gameModel);
                }
            }

            games_results = filtered_games;
        }

        return games_results;
    }

    // Return all games from the API and save them to database
    @Override
    public void updateDatabase() throws IOException {

        List<GameModel> games = callApi("https://free-to-play-games-database.p.rapidapi.com/api/games");

        // Save games to database
        gameRepository.saveAll(games);
    }

    // View details of selected game
    @Override
    public GameModel getGame(Long game_id) {

        // Select game from the database based on its id
        return gameRepository.findById(game_id)
                .orElseThrow(() -> {
                    String message = "Game with id " + game_id + " does not exist!";
                    return new IllegalStateException(message);
                });
    }

    // Add game to user's list
    @Transactional
    @Override
    public void saveGameToList(Long game_id) {

        // Get username of logged-in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Select user from the database
        UserModel user = userRepository.findById(username)
                .orElseThrow(() -> {
                    String message = "User with username " + username + " does not exist!";
                    return new IllegalStateException(message);
                });

        // Select game from the database
        GameModel game = gameRepository.findById(game_id)
                .orElseThrow(() -> {
                    String message = "Game with id " + game_id + " does not exist!";
                    return new IllegalStateException(message);
                });

        // Update user's game list
        if(!user.getGames().contains(game)){
            List<GameModel> games_list = user.getGames();
            games_list.add(game);
            user.setGames(games_list);
        }
    }

    // Return user's personal list of games
    @Override
    public List<GameModel> getUserGamesList() {

        // Get username of logged-in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Select user from the database
        UserModel user = userRepository.findById(username)
                .orElseThrow(() -> {
                    String message = "User with username " + username + " does not exist!";
                    return new IllegalStateException(message);
                });

        return user.getGames();
    }

    // Remove game from user's list
    @Transactional
    @Override
    public void deleteGameFromList(Long game_id) {

        // Get username of logged-in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Select user from the database
        UserModel user = userRepository.findById(username)
                .orElseThrow(() -> {
                    String message = "User with username " + username + " does not exist!";
                    return new IllegalStateException(message);
                });

        // Select game from the database
        GameModel game = gameRepository.findById(game_id)
                .orElseThrow(() -> {
                    String message = "Game with id " + game_id + " does not exist!";
                    return new IllegalStateException(message);
                });

        // Update user's game list
        if(user.getGames().contains(game)){
            List<GameModel> games_list = user.getGames();
            games_list.remove(game);
            user.setGames(games_list);
        }
    }
}
