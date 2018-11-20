package com.coin.app.controller;

import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.AccountService;
import com.coin.app.service.TransactionService;
import com.coin.app.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
//@CrossOrigin(origins = {"http://localhost:8080"}, maxAge = 4800, allowCredentials = "false")
public class UserAccountController
{
    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TransactionService transactionService;

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/userAccount")
    public ResultData userAccount()
    {
        return accountService.getUserAccount();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/accountTransactionsSize")
    public Long userAccountTransactionsSize()
    {
        return transactionService.countUserAccountTransactions();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/accountTransactions")
    public ResultData userAccountTransactions(@RequestBody Map<String, String> input)
    {
        return transactionService.getUserAccountTransactions(input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/sendCodeForWithdrawal")
    public ResultData sendCodeForWithdrawal(@RequestBody Map<String, String> input)
    {
        return emailService.sendWithdrawalCode(input.get("userId"));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/withdrawFromUserAccount")
    public ResultData withdrawFromUserAccount(@RequestBody Map<String, String> input)
    {
        return transactionService.transfer(input.get("userId"), input.get("address"), input.get("amount"), input.get("userSecurityCode"));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/sendTicket")
    public ResultData saveSupportTicket(@RequestBody Map<String, String> input)
    {
        return accountService.saveSupportTicket(input.get("subject"), input.get("description"));
    }

}
