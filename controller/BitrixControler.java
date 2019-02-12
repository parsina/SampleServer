package com.coin.app.controller;

import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.BitrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bitrix")
public class BitrixControler
{
    @Autowired
    private BitrixService bitrixService;

    @PostMapping("/authenticateReferee")
    public ResultData authenticateReferee(@RequestBody Map<String, String> input)
    {
        return bitrixService.authenticateReferee(input.get("referee"));
    }

    @PostMapping("/login")
    public ResultData login(@RequestBody Map<String, String> input)
    {
        return bitrixService.login(input.get("email"), input.get("password"));
    }

    @PostMapping("/signup")
    public ResultData signup(@RequestBody Map<String, String> input)
    {
        return bitrixService.signup(input.get("username"), input.get("email"), input.get("password"), input.get("referee"));
    }

    @PostMapping("/resendVerification")
    public ResultData resendVerification(@RequestBody Map<String, String> input)
    {
        return bitrixService.resendVerification(input.get("email"));
    }

    @PostMapping("/confirmVerification")
    public ResultData confirmVerification(@RequestBody Map<String, String> input)
    {
        return bitrixService.confirmVerification(input.get("token"));
    }

    @PostMapping("/forgotPassword")
    public ResultData forgotPassword(@RequestBody Map<String, String> input)
    {
        return bitrixService.forgotPassword(input.get("email"));
    }

    @PostMapping("/logout")
    public boolean logout(@RequestBody Map<String, ?> input)
    {
        System.out.println(input);
        return true;
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/userAccountData")
    public ResultData userAccountData()
    {
        return bitrixService.getUserAccountData();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/sendCodeForWithdrawal")
    public ResultData sendCodeForWithdrawal(@RequestBody Map<String, String> input)
    {
        return bitrixService.sendWithdrawalCode();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/transactionsSize")
    public Long userAccountTransactionsSize()
    {
        return bitrixService.countTransactions();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/transactions")
    public ResultData userAccountTransactions(@RequestBody Map<String, String> input)
    {
        return bitrixService.getTransactions(input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/findUserChildren")
    public ResultData findUserChildren(@RequestBody Map<String, Long> input)
    {
        return bitrixService.findUserData();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/activateNode")
    public ResultData activateUserNode()
    {
        return bitrixService.activateUserNode();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/sendBitcoin")
    public ResultData sendBitcoin(@RequestBody Map<String, String> input)
    {
        return bitrixService.sendBitcoin(input.get("userId"), input.get("address"), input.get("amount"), input.get("userSecurityCode"));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/completeMatrix")
    public ResultData completeMatrix(@RequestBody Map<String, String> input)
    {
        return bitrixService.completeMatrix();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/startNewPlan")
    public ResultData startNewPlan(@RequestBody Map<String, String> input)
    {
        return bitrixService.startNewPlan();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/changeUserPassword")
    public ResultData changeUserPassword(@RequestBody Map<String, String> input)
    {
        return bitrixService.changeUserPassword(input.get("currentPassword"), input.get("newPassword"), input.get("repeatedNewPassword"));
    }

}