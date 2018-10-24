package com.coin.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Transaction;
import com.coin.app.repository.TransactionRepository;
import com.coin.app.util.Utills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService
{
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserService userService;

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
}
