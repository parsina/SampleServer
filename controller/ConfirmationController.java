package com.coin.app.controller;

import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class ConfirmationController
{
    @Autowired
    private UserService userService;

    @PostMapping("/user")
    public ResultData confirmUser(String token)
    {
        return userService.confirmRegistration(token);
    }
}
