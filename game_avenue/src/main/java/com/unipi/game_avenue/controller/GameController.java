package com.unipi.game_avenue.controller;

import com.unipi.game_avenue.model.GameModel;
import com.unipi.game_avenue.model.SearchGameModel;
import com.unipi.game_avenue.service.implementation.GameServiceImpl;
import com.unipi.game_avenue.service.implementation.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameServiceImpl gameService;
    private final UserServiceImpl userService;

    // GET: search.html
    @GetMapping(path = "/search/{search_type}")
    public String search(@PathVariable("search_type") String search_type, Model model, HttpServletRequest request){

        // Set type of search (basic vs advanced)
        SearchGameModel game = new SearchGameModel();

        if(Objects.equals(search_type,"basic")){
            game.setBasic_search(true);
        } else if (Objects.equals(search_type,"advanced")) {
            game.setBasic_search(false);
        }

        // Create session variable
        request.getSession().setAttribute("basic_search", game.getBasic_search());

        // Add attribute to model
        model.addAttribute("game", game);

        // Add attribute to model
        List<String> platforms = Arrays.asList("all", "pc", "browser");
        model.addAttribute("platforms", platforms);

        // Add attribute to model
        List<String> genres = gameService.getAllTags();
        model.addAttribute("genres", genres);

        // Add attribute to model
        List<String> sort_by_list = Arrays.asList("release-date", "relevance", "popularity", "alphabetical");
        model.addAttribute("sort_by_list", sort_by_list);

        return "search";
    }

    //POST: search game
    @PostMapping(path = "/search")
    public String search(@Valid @ModelAttribute("game") SearchGameModel game, HttpServletRequest request) {

        // Create session variable
        request.getSession().setAttribute("game", game);

        return "redirect:/search_results";

    }

    //GET: result.html
    @GetMapping(path = "/search_results")
    public String result(Model model, HttpSession session) throws IOException {

        // Get variable from session
        SearchGameModel game = (SearchGameModel) session.getAttribute("game");

        // Get variable from session
        Boolean basic_search = (Boolean) session.getAttribute("basic_search");

        // Set type of search (basic vs advanced)
        game.setBasic_search(basic_search);

        // Get results games based on user's preferences
        List<GameModel> games = gameService.searchGames(game);

        // Add attribute to model
        model.addAttribute("games", games);

        // Add attribute to model
        model.addAttribute("basic_search", basic_search);

        return "result";
    }

    // GET: details.html
    @GetMapping(path = "/details/{game_id}")
    public String details(@PathVariable("game_id") Long id, Model model) {

        // Add attribute to model
        model.addAttribute("user_list", userService.getCurrentUser().getGames());

        // Add attribute to model
        model.addAttribute("game", gameService.getGame(id));

        return "details";
    }

    // GET: save game to personal list
    @GetMapping(path = "/save/{game_id}")
    public String save(@PathVariable("game_id") Long id){

        gameService.saveGameToList(id);

        return "redirect:/details/" + id.toString() + "?saved";
    }

    // GET: remove game from personal list
    @GetMapping(path = "/remove/{game_id}")
    public String remove(@PathVariable("game_id") Long id){

        gameService.deleteGameFromList(id);

        return "redirect:/details/" + id.toString() + "?removed";
    }

    // GET: profile.html
    @GetMapping(path = "/profile")
    public String profile(Model model){

        // Get user's saved games
        List<GameModel> games = gameService.getUserGamesList();

        // Add attribute to model
        model.addAttribute("games", games);

        return "profile";
    }
}
