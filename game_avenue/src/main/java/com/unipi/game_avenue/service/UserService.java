package com.unipi.game_avenue.service;

import com.unipi.game_avenue.model.FormUserModel;
import com.unipi.game_avenue.model.UserModel;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.List;

public interface UserService {

    BindingResult registerUser(FormUserModel user, BindingResult result) throws IOException;

    Boolean verifyEmail(String email) throws IOException;

    Boolean verifyPhoneNumber(String phone_number) throws IOException;

    List<UserModel> getAllUsers();

    void deleteUser(String username);

    UserModel getCurrentUser();
}
