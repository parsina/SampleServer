package com.coin.app.service;

import com.google.gson.JsonArray;

public interface LiveScoreService
{
    void loadData();

    JsonArray getLiveScores();
}
