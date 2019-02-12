package com.coin.app.service.mail;

import com.coin.app.dto.data.ResultData;

public interface EmailService
{
    void sendVerification(String email, String token);

    ResultData sendWithdrawalCode(String email, String code);

    void sendNewPassword(String email, String password);
}
