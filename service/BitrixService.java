package com.coin.app.service;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Bitrix;

public interface BitrixService
{
    void initializeDataBase();

    ResultData signup(String username, String email, String password, String referee);

    ResultData login(String email, String password);

    ResultData authenticateReferee(String referee);

    ResultData resendVerification(String email);

    ResultData confirmVerification(String token);

    ResultData forgotPassword(String email);

    Long countTransactions();

    ResultData getTransactions(String filter, String sortOrder, String sortBy, int pageNumber,int pageSize);

    ResultData getUserAccountData();

    ResultData findUserData();

    ResultData activateUserNode();

    Bitrix getCurrentUser();

    Bitrix getWallet();

    Bitrix getFund();

    Bitrix getAdmin();

    Bitrix getStarter();

    Bitrix getBronze();

    Bitrix getSilver();

    Bitrix getGold();

    ResultData sendWithdrawalCode();

    ResultData sendBitcoin(String userId, String address, String amount, String userSecurityCode);

    ResultData completeMatrix();

    ResultData startNewPlan();

    ResultData changeUserPassword(String currentPassword, String newPassword, String repeatedNewPassword);
}
