package com.coin.app.service;


import java.util.Date;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.SupportTicket;
import com.coin.app.model.User;
import com.coin.app.model.enums.AccountStatus;
import com.coin.app.model.enums.SupportTicketStatus;
import com.coin.app.repository.AccountRepository;
import com.coin.app.repository.SupportTicketRepository;
import com.coin.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService
{
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Override
    public Account createAccount(User user)
    {
        Account account = new Account();
        account.setCreatedDate(new Date());
        account.setStatus(AccountStatus.INACTIVE);
        account.setWallet(walletService.createWallet(user));
        account.setUser(user);
        return accountRepository.save(account);
    }

    @Override
    public ResultData getUserAccount()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.isAuthenticated())
        {
            Account account = userRepository.findByUsername(authentication.getName()).getAccount();
            ResultData data = new ResultData(true, "");
            data.addProperty("accountId", account.getId());
            data.addProperty("accountStatus", account.getStatus());
            data.addProperty("walletAddress", account.getWallet().getAddress());
            data.addProperty("balance", account.getWallet().getBalance());
            return data;
        }

        return new ResultData(false, "عدم وجود امکان دسترسی به اطلاعات حساب کاربری");
    }

    @Override
    public ResultData saveSupportTicket(String subject, String description)
    {
        if(subject == null || subject.trim().equals(""))
            return new ResultData(false, "subject");
        if(description == null || description.trim().equals(""))
            return new ResultData(false, "description");

        SupportTicket supportTicket = new SupportTicket();
        supportTicket.setCreatedDate(new Date());
        supportTicket.setUpdateDate(new Date());
        supportTicket.setSender(userService.getCurrentUser());
        supportTicket.setSubject(subject);
        supportTicket.setDescription(description);
        supportTicket.setStatus(SupportTicketStatus.OPEN);
        supportTicketRepository.save(supportTicket);
        return new ResultData(true, "");
    }
}
