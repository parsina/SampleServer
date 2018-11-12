package com.coin.app.service;

import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.User;

public interface UserService
{
    ResultData createUser(String username, String email, String password, String repeatedPassword);

    ResultData confirmRegistration(String token);

    ResultData login(String email, String password);

    boolean isAuthenticated(Long userId);

    User getCurrentUser();

    ResultData sendInvitations(List<String> emails);

    User findByEmail(String email);

    User findByUserName(String username);

    User findById(Long id);

    User saveUser(User user);
}
