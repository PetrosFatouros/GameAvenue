package com.unipi.game_avenue.controller;

import com.unipi.game_avenue.model.FormUserModel;
import com.unipi.game_avenue.service.implementation.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserServiceImpl userService;

    // GET: home.html
    @GetMapping
    public String index(){
        return "home";
    }

    // GET: home.html
    @GetMapping(path = "/home")
    public String home(){
        return "home";
    }

    // GET: login.html
    @GetMapping(path = "/login")
    public String login(Model model){

        // Add attribute to model
        model.addAttribute("user", new FormUserModel());

        return "login";
    }

    // GET: register.html
    @GetMapping(path = "/register")
    public String register(Model model){

        // Add attribute to model
        model.addAttribute("user", new FormUserModel());

        return "register";
    }

    //POST: register user
    @PostMapping(path = "/register")
    public String registerUser(@Valid @ModelAttribute("user") FormUserModel user, BindingResult result) throws IOException {

        result = userService.registerUser(user, result);

        if (result.hasErrors()) {
            return "register";
        }
        else{
            return "redirect:/register?success";
        }
    }
}
