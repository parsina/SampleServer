package com.coin.app.service.mail;

import com.coin.app.dto.data.ResultData;

public interface EmailService
{
    void sendActivationLink(String email);

    void sendInvitationEmail(String email);

    ResultData sendWithdrawalCode(String userId);

    void sendNewPassword(String email, String password);
}
