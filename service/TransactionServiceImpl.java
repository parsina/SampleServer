package com.coin.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.Transaction;
import com.coin.app.model.Wallet;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.repository.AccountRepository;
import com.coin.app.repository.TransactionRepository;
import com.coin.app.repository.WalletRepository;
import com.coin.app.util.Utills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService
{
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BitcoinJService bitcoinJService;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Long countUserAccountTransactions()
    {
        return transactionRepository.countByAccount(userService.getCurrentUser().getAccount());
    }

    @Override
    public ResultData getUserAccountTransactions(String filter, String sortOrder, String sortBy, int pageNumber, int pageSize)
    {
        ResultData data = new ResultData(true, "");
        List<ResultData> transactionData = new ArrayList<>();

        List<String> sorts = new ArrayList<>();
        if(sortBy.isEmpty() || sortBy.equals("updateDate"))
        {
            sorts.add("updateDate");
            sorts.add("updateTime");
        }
        else
        if(sortBy.equals("value"))
        {
            sorts.add("totalValue");
        }
        else
            sorts.add(sortBy);
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sorts);

        for (Transaction transaction : transactionRepository.findByAccount(userService.getCurrentUser().getAccount(), PageRequest.of(pageNumber, pageSize, orderBy)))
        {
            ResultData trData = new ResultData(true, "");
            trData.addProperty("createdDat", Utills.nameDisplayForDate(transaction.getCreatedDate(), true));
            trData.addProperty("createdTime", transaction.getCreatedTime().toString());

            trData.addProperty("updateDat", Utills.nameDisplayForDate(transaction.getUpdateDate(), true));
            trData.addProperty("updateTime", transaction.getUpdateTime().toString());

            trData.addProperty("description", transaction.getDescription());
            trData.addProperty("value", transaction.getTotalValue());
            trData.addProperty("fee", transaction.getFee());

            trData.addProperty("status", transaction.getStatus().name());
            trData.addProperty("type", transaction.getType().name());
            transactionData.add(trData);
        }
        data.addProperty("transactions", transactionData);
        return data;
    }

    @Override
    public ResultData transfer(String userId, String address, String amountValue, String userSecurityCode)
    {
        ResultData resultData = new ResultData(true, "");
        if (userService.getCurrentUser().getId().equals(Long.valueOf(userId)))
        {
            String securityCode = userService.getCurrentUser().getAccount().getDescription();
            if (!userSecurityCode.trim().equals(securityCode.trim()))
                return new ResultData(false, "کد امنیتی صحیح نمی باشد");

            if (Long.valueOf(amountValue) < 20000)
                return new ResultData(false, "مبلغ کمتر از حد مجاز می باشد");

            Wallet wallet = userService.getCurrentUser().getAccount().getWallet();
            Long amount = Long.valueOf(amountValue);
            Long balance = Long.valueOf(wallet.getBalance());
            Long fee = 1000 + Math.round(amount * 0.005);

            if (amount > balance - fee)
                return new ResultData(false, "مبلغ بیشتر از حد مجاز می باشد");

            TransactionStatus status = TransactionStatus.CONFIRMED;
            Wallet destinationWallet = walletRepository.findByAddress(address);
            if (destinationWallet == null)
                status = bitcoinJService.forwardCoins(amount, address); //Forward coins to user external wallet
            else
            {
                // Create destination user wallet transaction
                Transaction destinationUserTransaction = new Transaction();
                destinationUserTransaction.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
                destinationUserTransaction.setCreatedTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
                destinationUserTransaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
                destinationUserTransaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
                Account toAccount = accountRepository.findByWallet(destinationWallet);
                destinationUserTransaction.setTxId("TRANSFER-FROM-" + userService.getCurrentUser().getId()  + "-TO-" + toAccount.getUser().getId() + "-" + transactionRepository.count());
                destinationUserTransaction.setType(TransactionType.DEPOSIT);
                destinationUserTransaction.setTotalValue(amount);
                destinationUserTransaction.setStatus(status);
                destinationUserTransaction.setDescription("واریز به حساب شما از طرف کاربر " +  userService.getCurrentUser().getUsername());
                destinationUserTransaction.setAccount(toAccount);
                transactionRepository.save(destinationUserTransaction);

                //Set destination user new balance value
                Long newBalance = Long.valueOf(destinationWallet.getBalance()) + amount;
                destinationWallet.setBalance(newBalance + "");
                walletRepository.save(destinationWallet);
            }

            // Create user transaction
            Transaction userTransaction = new Transaction();
            userTransaction.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setCreatedTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setTxId("WITHDRAW-BY-" + userService.getCurrentUser().getId() + "-" + transactionRepository.count());
            userTransaction.setType(TransactionType.WITHDRAWAL);
            userTransaction.setTotalValue(amount);
            userTransaction.setFee(fee + "");
            userTransaction.setStatus(status);
            userTransaction.setDescription("برداشت از حساب و انتقال به آدرس " + address);
            userTransaction.setAccount(userService.getCurrentUser().getAccount());
            transactionRepository.save(userTransaction);

            if (!userTransaction.getStatus().equals(TransactionStatus.FAILED))
            {
                //Add fee to admin balance
                Wallet adminWallet = userService.findByUserName("Admin").getAccount().getWallet();
                Long adminBalance = Long.valueOf(adminWallet.getBalance()) + fee;
                adminWallet.setBalance(adminBalance.toString());
                walletRepository.save(adminWallet);

                //Set user new balance value
                Long newBalance = balance - amount - fee;
                wallet.setBalance(newBalance + "");
                walletRepository.save(wallet);
                resultData.addProperty("balance", wallet.getBalance());
                return resultData;
            }
            return new ResultData(false, "اشکال در ارسال");
        }
        return new ResultData(false, "اشکال در تشخیص هویت");
    }
}
