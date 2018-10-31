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

    User activateUser(User user);

    void save(User user);

    User findByEmail(String email);

    User findByUserName(String username);

    List<User> findAllUsers();

    User findById(Long id);

    boolean isUserExist(User user);

    User saveUser(User user);

    User updateUser(User user);

    void deleteUserById(Long id);

    void deleteAllUsers();
}
