package com.coin.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Transaction;
import com.coin.app.model.Wallet;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
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

    @Override
    public Long countUserAccountTransactions()
    {
        return transactionRepository.countByAccount(userService.getCurrentUser().getAccount());
    }

    @Override
    public ResultData getUserAccountTransactions(String filter, String sortOrder, String sortBy, int pageNumber,int pageSize)
    {
        ResultData data = new ResultData(true, "");
        List<ResultData> transactionData = new ArrayList<>();
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.isEmpty() ? "id" : sortBy);
        for (Transaction transaction : transactionRepository.findByAccount(userService.getCurrentUser().getAccount(), PageRequest.of(pageNumber, pageSize, orderBy)))
        {
            ResultData trData = new ResultData(true, "");
            trData.addProperty("createdDat", Utills.nameDisplayForDate(transaction.getCreatedDate(), true));
            trData.addProperty("createdTime", transaction.getCreatedTime().toString());

            trData.addProperty("updateDat", Utills.nameDisplayForDate(transaction.getUpdateDate(), true));
            trData.addProperty("updateTime", transaction.getUpdateTime().toString());

            trData.addProperty("description", transaction.getDescription());
            trData.addProperty("value", transaction.getTotalValue());

            trData.addProperty("status", transaction.getStatus().name());
            trData.addProperty("type", transaction.getType().name());
            transactionData.add(trData);
        }
        data.addProperty("transactions", transactionData);
        return data;
    }

    @Override
    public ResultData transfer(String userId, String address, String amountValue, String securityCode, String userSecurityCode)
    {
        ResultData resultData = new ResultData(true, "");
        if(userService.getCurrentUser().getId().equals(Long.valueOf(userId)))
        {
            if(!userSecurityCode.equals(securityCode))
                return new ResultData(false, "Security code is wrong !");

            if(Long.valueOf(amountValue) < 20000)
                return new ResultData(false, "Amount is under 20000 !");

            Wallet wallet = userService.getCurrentUser().getAccount().getWallet();
            Long amount = Long.valueOf(amountValue);
            Long balance = Long.valueOf(wallet.getBalance());
            Long fee = 1000 + Math.round(amount * 0.005);

            if(amount > balance - fee)
                return new ResultData(false, "Amount is out of balance !");

            //Forward coins to user external wallet
            TransactionStatus status = bitcoinJService.forwardCoins(amount, address);

            // Create user transaction
            Transaction userTransaction = new Transaction();
            userTransaction.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setCreatedTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            userTransaction.setTxId("TRANSFER-TO-" + userService.getCurrentUser().getId() + "-" + transactionRepository.count());
            userTransaction.setType(TransactionType.WITHDRAWAL);
            userTransaction.setTotalValue(amount);
            userTransaction.setFee(fee + "");
            userTransaction.setStatus(status);
            userTransaction.setDescription("انتقال به آدرس " + address);
            userTransaction.setAccount(userService.getCurrentUser().getAccount());
            transactionRepository.save(userTransaction);

            if(!userTransaction.getStatus().equals(TransactionStatus.FAILED))
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
            return new ResultData(false, "Transfering Problem !!");
        }
        return new ResultData(false, "Authentication Problem !!");
    }
}
