package com.coin.app.controller;

import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.User;
import com.coin.app.service.BinanceService;
import com.coin.app.service.UserService;
import com.coin.app.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping({"/api"})
public class UserController
{
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultData create(@RequestBody Map<String, ?> input)
    {
        return userService.createUser(input.get("username").toString(), input.get("pass").toString(), input.get("reppass").toString());
    }

    @GetMapping(path = {"/{id}"})
    public User findOne(@PathVariable("id") Long id)
    {
        return userService.findById(id);
    }

    @PutMapping
    public User update(@RequestBody User user)
    {
//        return userService.update(user);
        return userService.findById(user.getId());
    }

    @DeleteMapping(path = {"/{id}"})
    public User delete(@PathVariable("id") Long id)
    {
//        return userService.delete(id);
        return userService.findById(id);
    }

    @GetMapping
    public List findAll()
    {
        return userService.findAllUsers();
    }
}
