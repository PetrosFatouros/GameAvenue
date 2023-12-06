package com.unipi.game_avenue.service.implementation;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.unipi.game_avenue.model.*;
import com.unipi.game_avenue.repository.RoleRepository;
import com.unipi.game_avenue.repository.UserRepository;
import com.unipi.game_avenue.service.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    // Register user
    @Override
    public BindingResult registerUser(FormUserModel user, BindingResult result) throws IOException {

        // Update user's roles
        if(user.getIsUser() == null){
            user.setIsUser(false);
        }
        if(user.getIsAdmin() == null){
            user.setIsAdmin(false);
        }
        if(!user.getIsUser() && !user.getIsAdmin()){
            user.setIsUser(true);
        }

        // Passwords must match
        if(!Objects.equals(user.getPassword(),user.getRepeat_password())){
            String message = "Error: Passwords must match!";
            result.rejectValue("repeat_password", "error.user", message);
        }

        // Check if another user with the same username already exists in the database
        boolean existsByUsername = userRepository.existsById(user.getUsername());
        if (existsByUsername) {
            String message = "Error: User with username " + user.getUsername() + " already exists!";
            result.rejectValue("username", "error.user", message);
        }

        // Verify email
        // https://rapidapi.com/mr_admin/api/email-verifier/
        if(verifyEmail(user.getEmail())){

            // Check if another user with the same email already exists in the database
            boolean existsByEmail = userRepository.existsByEmail(user.getEmail());
            if (existsByEmail) {
                String message = "Error: User with email " + user.getEmail() + " already exists!";
                result.rejectValue("email", "error.user", message);
            }
        }
        else{
            String message = "Error: Email " + user.getEmail() + " is not valid!";
            result.rejectValue("email", "error.user", message);
        }

        // Verify phone number
        // https://rapidapi.com/Veriphone/api/veriphone/
        if(verifyPhoneNumber(user.getPhoneNumber())){
            // Check if another user with the same phone number already exists in the database
            boolean existsByPhoneNumber = userRepository.existsByPhoneNumber(user.getPhoneNumber());
            if (existsByPhoneNumber) {
                String message = "Error: User with phone number " + user.getPhoneNumber() + " already exists!";
                result.rejectValue("phoneNumber", "error.user", message);
            }
        }
        else {
            String message = "Error: Phone number " + user.getPhoneNumber() + " is not valid!";
            result.rejectValue("phoneNumber", "error.user", message);
        }

        // Set up user's roles
        Set<RoleModel> roles = new HashSet<>();
        if(!user.getIsUser() && !user.getIsAdmin()){
            String message = "Error: User must have at least one role!";
            result.rejectValue("isAdmin", "error.user", message);
        }
        else {
            if (user.getIsUser()) {
                RoleModel userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
            if (user.getIsAdmin()) {
                RoleModel adminRole = roleRepository.findByName(RoleEnum.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(adminRole);
            }
        }

        if (!result.hasErrors()) {

            // Create new user's account
            UserModel userModel = new UserModel(
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    encoder.encode(user.getPassword()),
                    roles,
                    new ArrayList<>()
            );

            // Insert user into the database
            userRepository.save(userModel);
        }

        return result;
    }

    // Verify user's email using API
    // https://rapidapi.com/Top-Rated/api/e-mail-check-invalid-or-disposable-domain/
    @Override
    public Boolean verifyEmail(String email) throws IOException {

        // Prepare email
        email = email.replace("@", "%40");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://mailcheck.p.rapidapi.com/?domain=" + email)
                .get()
                .addHeader("X-RapidAPI-Key", "85d7bede2fmsh905c6165a0954bbp1b65e5jsn2e7b36cdd213")
                .addHeader("X-RapidAPI-Host", "mailcheck.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();

        String responseBody = response.body().string();

        JSONObject json_response = new JSONObject(responseBody);

        boolean valid = json_response.getBoolean("valid");
        boolean block = json_response.getBoolean("block");
        boolean disposable = json_response.getBoolean("disposable");

        return valid && !block && !disposable;
    }

    // Verify user's phone number using API
    // https://rapidapi.com/Veriphone/api/veriphone/
    @Override
    public Boolean verifyPhoneNumber(String phone_number) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://veriphone.p.rapidapi.com/verify?phone=" + phone_number)
                .get()
                .addHeader("X-RapidAPI-Key", "85d7bede2fmsh905c6165a0954bbp1b65e5jsn2e7b36cdd213")
                .addHeader("X-RapidAPI-Host", "veriphone.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();

        String responseBody = response.body().string();

        JSONObject json_response = new JSONObject(responseBody);

        return json_response.getBoolean("phone_valid");
    }

    // Return all users based on role
    @Override
    public List<UserModel> getAllUsers() {

        // Select all users from the database
        return userRepository.findAll();
    }

    // Delete user based on username
    @Override
    public void deleteUser(String username) {

        // True if user exists, False otherwise
        boolean exists = userRepository.existsById(username);

        // If user does not exist, throw exception
        if (!exists) {
            String message = "User with username " + username + " does not exist!";
            throw new IllegalStateException(message);
        }

        // Delete user from the database
        userRepository.deleteById(username);
    }

    // Select user from the database
    @Override
    public UserModel getCurrentUser() {

        // Get username of logged-in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findById(username)
                .orElseThrow(() -> {
                    String message = "User with username " + username + " does not exist!";
                    return new IllegalStateException(message);
                });
    }
}
