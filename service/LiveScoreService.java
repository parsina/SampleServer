package com.coin.app.service;

import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.google.gson.JsonArray;

public interface LiveScoreService
{
    void loadFixtures();

    List<ResultData> findAllFreeFixtures();

    JsonArray getLiveScores();

    void loadCountries();

    void loadLeague();

    void loadMatches();

    void loadLiveScores();
}
