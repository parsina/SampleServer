package com.coin.app.service;


import com.coin.app.model.Account;
import com.coin.app.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public interface AccountService
{
    Account createAccount(User user);
}
