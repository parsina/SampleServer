package com.coin.app.controller;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class UserAccountController
{
    @Autowired
    private AccountService accountService;

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/userAccount")
    public ResultData userAccount()
    {
        return accountService.getUserAccount();
    }
}
