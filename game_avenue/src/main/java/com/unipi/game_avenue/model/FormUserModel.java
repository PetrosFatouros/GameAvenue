package com.unipi.game_avenue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FormUserModel {

    @Size(min = 3, max = 15)
    private String username;

    @Size(min = 3, max = 320)
    @Email(message = "Invalid email!")
    private String email;

    @Size(min = 5, max = 15)
    @NumberFormat(style = NumberFormat.Style.NUMBER)
    private String phoneNumber;

    @Size(min = 6, max = 20)
    private String password;

    @Size(min = 6, max = 20)
    private String repeat_password;

    private Boolean isUser;

    private Boolean isAdmin;
}
