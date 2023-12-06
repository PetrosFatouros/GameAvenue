package com.unipi.game_avenue.controller;

import com.unipi.game_avenue.model.FormUserModel;
import com.unipi.game_avenue.service.implementation.GameServiceImpl;
import com.unipi.game_avenue.service.implementation.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userService;
    private final GameServiceImpl gameService;

    // GET: users.html
    @GetMapping(path = "/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String users(Model model){
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    //GET: delete user
    @GetMapping ("/delete/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable("username") String username){
        userService.deleteUser(username);
        return "redirect:/users";
    }

    // GET: update database (save all games in the API to the database)
    @GetMapping(path = "/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateDatabase() throws IOException {

        gameService.updateDatabase();

        return "redirect:/home?success";
    }
}
