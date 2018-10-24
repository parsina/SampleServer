package com.coin.app.service;

import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.google.gson.JsonArray;

public interface LiveScoreService
{
    void loadFixtures();

    void loadFixtureBooks();

    List<ResultData> findAllFreeFixtures();

    void updateFixtureData(String fixtureIds);

    List<ResultData> getCountries(boolean onlyWithFarsiName);

    void saveCountry(String key, String value);

    List<ResultData> getLeagues(boolean onlyWithFarsiName);

    void saveLeague(String key, String value);

    List<ResultData> getTeams(boolean onlyWithFarsiName);

    void saveTeam(String key, String value);

}
