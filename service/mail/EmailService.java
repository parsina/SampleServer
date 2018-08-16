package com.coin.app.service.mail;

import com.coin.app.model.User;

public interface EmailService
{
    void sendActivationLink(String email);
}
