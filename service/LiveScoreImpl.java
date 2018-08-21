package com.coin.app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import com.coin.app.model.livescore.Country;
import com.coin.app.model.livescore.Match;
import com.coin.app.model.livescore.League;
import com.coin.app.repository.CountryRepository;
import com.coin.app.repository.MatchRepository;
import com.coin.app.repository.LeagueRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LiveScoreImpl implements LiveScore
{
    private static String root = "http://livescore-api.com/api-client/";
    private static String apiKey = ".json?key=8Tm4gJskBmOoLKfO";
    private static String secretKey = "&secret=nVO5RtTvDBcXeMYmFkrW4UgFh432PjRd";

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Override
    public void loadData()
    {
        loadCountries();
        loadLeague();
        loadMatches();
    }

    private void loadCountries()
    {
        String content = fetchContent(root + "countries/list.json" + apiKey + secretKey);
        JsonObject jsonObject = toJsonObject(content);
        if(jsonObject.get("success").getAsBoolean())
        {
            JsonArray jsonCountries = jsonObject.get("data").getAsJsonObject().get("country").getAsJsonArray();
            for(JsonElement element : jsonCountries)
            {
                Optional<Country> countries = countryRepository.findById(element.getAsJsonObject().get("id").getAsLong());
                if(!countries.isPresent())
                {
                    Country country = new Country();
                    country.setId(element.getAsJsonObject().get("id").getAsLong());
                    country.setName(element.getAsJsonObject().get("name").getAsString());
                    country.setReal(element.getAsJsonObject().get("is_real").getAsInt());
                    countryRepository.save(country);
                }
            }
        }
    }

    public void loadLeague()
    {
        String content = fetchContent(root + "leagues/list.json" + apiKey + secretKey);
        JsonObject jsonObject = toJsonObject(content);
        if(jsonObject.get("success").getAsBoolean())
        {
            JsonArray jsonLeagues = jsonObject.get("data").getAsJsonObject().get("league").getAsJsonArray();
            for(JsonElement element : jsonLeagues)
            {
                Optional<League> leagues = leagueRepository.findById(element.getAsJsonObject().get("id").getAsLong());
                if(!leagues.isPresent())
                {
                    League league = new League();
                    league.setId(element.getAsJsonObject().get("id").getAsLong());
                    league.setName(element.getAsJsonObject().get("name").getAsString());
                    countryRepository.findById(element.getAsJsonObject().get("country_id").getAsLong()).ifPresent(league::setCountry);
                    leagueRepository.save(league);
                }
            }
        }
    }

    public void loadMatches()
    {
        for(int i=1 ; i < 10; i++)
        {
            String page = "&page=" + i;
            String content = fetchContent(root + "fixtures/matches.json" + apiKey + secretKey + page);
            JsonObject jsonObject = toJsonObject(content);
            if (jsonObject.get("success").getAsBoolean())
            {
                JsonArray jsonFixtures = jsonObject.get("data").getAsJsonObject().get("fixtures").getAsJsonArray();
                for (JsonElement element : jsonFixtures)
                {
                    Optional<Match> matches = matchRepository.findById(element.getAsJsonObject().get("id").getAsLong());
                    if (!matches.isPresent())
                    {
                        Match match = new Match();
                        match.setId(element.getAsJsonObject().get("id").getAsLong());
                        match.setDate(element.getAsJsonObject().get("date").getAsString());
                        match.setTime(element.getAsJsonObject().get("time").getAsString());
                        match.setRound(element.getAsJsonObject().get("round").getAsString());
                        match.setHomeName(element.getAsJsonObject().get("home_name").getAsString());
                        match.setAwayName(element.getAsJsonObject().get("away_name").getAsString());
                        match.setLocation(element.getAsJsonObject().get("location").getAsString());
                        leagueRepository.findById(element.getAsJsonObject().get("league_id").getAsLong()).ifPresent(match::setLeague);
                        matchRepository.save(match);
                    }
                }
            }
        }
    }

    private static String fetchContent(String uri)
    {
        try
        {
            final int OK = 200;
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
                return response.toString();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private JsonObject toJsonObject(String jsonStr)
    {
        return new JsonParser().parse(jsonStr).getAsJsonObject();
    }
}
