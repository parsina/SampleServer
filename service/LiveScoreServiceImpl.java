package com.coin.app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.util.Utills;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ir.huri.jcal.JalaliCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LiveScoreServiceImpl implements LiveScoreService
{
    @Value("${api.root}")
    private String root;

    @Value("${api.key}")
    private String key;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Override
    public void loadFixtures()
    {
//        if(LocalDate.now().getDayOfWeek() != DayOfWeek.MONDAY)
//            return;

        String fDate = LocalDate.now().plusDays(3).toString();
        String tDate = LocalDate.now().plusDays(15).toString();

        String uri = this.root + "fixtures/between/" + fDate + "/" + tDate + "?api_token=" + this.key + "&include=localTeam,visitorTeam,league,inplay";
        String content = fetchContent(uri);
        JsonObject jsonObject = toJsonObject(content);
        if (jsonObject.get("data") != null)
        {
            JsonArray dataArray = jsonObject.get("data").getAsJsonArray();
            for (JsonElement element : dataArray)
            {
                Fixture fixture = fixtureRepository.findById(element.getAsJsonObject().get("id").getAsLong()).isPresent() ?
                        fixtureRepository.findById(element.getAsJsonObject().get("id").getAsLong()).get() :
                        new Fixture();

                if (fixture.getId() == null)
                    fixture.setId(element.getAsJsonObject().get("id").getAsLong());
                String date_time = element.getAsJsonObject().get("time").getAsJsonObject().get("starting_at").getAsJsonObject().get("date_time").getAsString();
                fixture.setLocalDate(LocalDate.parse(date_time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                String date = element.getAsJsonObject().get("time").getAsJsonObject().get("starting_at").getAsJsonObject().get("date").getAsString();
                fixture.setDate(new JalaliCalendar(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toString());
                fixture.setTime(element.getAsJsonObject().get("time").getAsJsonObject().get("starting_at").getAsJsonObject().get("time").getAsString());
                fixture.setLeagueId(element.getAsJsonObject().get("league_id").getAsLong());
                fixture.setLeagueName(element.getAsJsonObject().get("league").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString());
                fixture.setLocalTeamId(element.getAsJsonObject().get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("id").getAsLong());
                fixture.setLocalTeamCountryId(element.getAsJsonObject().get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("country_id").getAsLong());

                String countryURI = this.root + "countries/" + fixture.getLocalTeamCountryId() + "?api_token=" + this.key;
                String countryContent = fetchContent(countryURI);
                JsonObject countryObject = toJsonObject(countryContent);
                fixture.setLocalCountryName(countryObject.get("data").getAsJsonObject().get("name").getAsString());
                fixture.setLocalCountryFIFAName(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("fifa").getAsString());
                fixture.setLocalCountryFlag(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("flag").getAsString());


                fixture.setLocalTeamName(element.getAsJsonObject().get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString());
                fixture.setLocalTeamLogo(element.getAsJsonObject().get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("logo_path").getAsString());
                fixture.setVisitorTeamId(element.getAsJsonObject().get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("id").getAsLong());
                fixture.setVisitorTeamCountryId(element.getAsJsonObject().get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("country_id").getAsLong());
                fixture.setVisitorTeamName(element.getAsJsonObject().get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString());
                fixture.setVisitorTeamLogo(element.getAsJsonObject().get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("logo_path").getAsString());

                countryURI = this.root + "countries/" + fixture.getVisitorTeamCountryId() + "?api_token=" + this.key;
                countryContent = fetchContent(countryURI);
                countryObject = toJsonObject(countryContent);
                fixture.setVisitorCountryName(countryObject.get("data").getAsJsonObject().get("name").getAsString());
                fixture.setVisitorCountryFIFAName(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("fifa").getAsString());
                fixture.setVisitorCountryFlag(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("flag").getAsString());
                fixtureRepository.save(fixture);
            }
        }
    }

    @Override
    public List<ResultData> findAllFreeFixtures()
    {
        List<ResultData> results = new ArrayList<>();

        List<Fixture> fixtures = fixtureRepository.findByUsedAndLocalDateGreaterThanOrderByDateAscTimeAsc(false, LocalDate.now());

        for (Fixture fixture : fixtures)
        {
            ResultData result = new ResultData(true, "");
            result.addProperty("id", fixture.getId());
            result.addProperty("checked", false);
            result.addProperty("date", Utills.nameDisplayForDate(fixture.getDate(), true));
            result.addProperty("time", Utills.shortDisplayForTime(fixture.getTime()));
            result.addProperty("league", fixture.getLeagueName());
            result.addProperty("homeTeam", fixture.getLocalTeamName());
            result.addProperty("homeLogo", fixture.getLocalTeamLogo());
            result.addProperty("homeCountry", fixture.getLocalCountryName());
            result.addProperty("awayTeam", fixture.getVisitorTeamName());
            result.addProperty("awayLogo", fixture.getVisitorTeamLogo());
            result.addProperty("awayCountry", fixture.getVisitorCountryName());
            results.add(result);
        }
        return results;
    }

    @Override
    public JsonArray getLiveScores()
    {
//        String content = fetchContent(root + "scores/live" + apiKey + secretKey);
//        JsonObject jsonObject = toJsonObject(content);
//        if (jsonObject.get("success").getAsBoolean())
//            return jsonObject.get("data").getAsJsonObject().get("match").getAsJsonArray();
        return null;
    }

    @Override
    public void loadCountries()
    {
//        String content = fetchContent(root + "countries/list" + apiKey + secretKey);
//        JsonObject jsonObject = toJsonObject(content);
//        if (jsonObject.get("success").getAsBoolean())
//        {
//            JsonArray jsonCountries = jsonObject.get("data").getAsJsonObject().get("country").getAsJsonArray();
//            for (JsonElement element : jsonCountries)
//            {
//                Optional<Country> countries = countryRepository.findById(element.getAsJsonObject().get("id").getAsLong());
//                if (!countries.isPresent())
//                {
//                    Country country = new Country();
//                    country.setId(element.getAsJsonObject().get("id").getAsLong());
//                    country.setName(element.getAsJsonObject().get("name").getAsString());
//                    country.setReal(element.getAsJsonObject().get("is_real").getAsInt());
//                    countryRepository.save(country);
//                }
//            }
//        }
    }

    @Override
    public void loadLeague()
    {
//        String content = fetchContent(root + "leagues/list" + apiKey + secretKey);
//        JsonObject jsonObject = toJsonObject(content);
//        if (jsonObject.get("success").getAsBoolean())
//        {
//            JsonArray jsonLeagues = jsonObject.get("data").getAsJsonObject().get("league").getAsJsonArray();
//            for (JsonElement element : jsonLeagues)
//            {
//                Optional<League> leagues = leagueRepository.findById(element.getAsJsonObject().get("id").getAsLong());
//                if (!leagues.isPresent())
//                {
//                    League league = new League();
//                    league.setId(element.getAsJsonObject().get("id").getAsLong());
//                    league.setName(element.getAsJsonObject().get("name").getAsString());
//                    countryRepository.findById(element.getAsJsonObject().get("country_id").getAsLong()).ifPresent(league::setCountry);
//                    leagueRepository.save(league);
//                }
//            }
//        }
    }

    @Override
    public void loadMatches()
    {
//        for (int i = 1; i < 10; i++)
//        {
//            String page = "&page=" + i;
//            String content = fetchContent(root + "fixtures/matches" + apiKey + secretKey + page);
//            JsonObject jsonObject = toJsonObject(content);
//            if (jsonObject.get("success").getAsBoolean())
//            {
//                JsonArray jsonFixtures = jsonObject.get("data").getAsJsonObject().get("fixtures").getAsJsonArray();
//                for (JsonElement element : jsonFixtures)
//                {
//                    Optional<Match> matches = matchRepository.findById(element.getAsJsonObject().get("id").getAsLong());
//                    if (!matches.isPresent())
//                    {
//                        Match match = new Match();
//                        match.setId(element.getAsJsonObject().get("id").getAsLong());
//                        match.setDate(element.getAsJsonObject().get("date").getAsString());
//                        match.setTime(element.getAsJsonObject().get("time").getAsString());
//                        match.setRound(element.getAsJsonObject().get("round").getAsString());
//                        match.setHomeName(element.getAsJsonObject().get("home_name").getAsString());
//                        match.setAwayName(element.getAsJsonObject().get("away_name").getAsString());
//                        match.setLocation(element.getAsJsonObject().get("location").getAsString());
//                        leagueRepository.findById(element.getAsJsonObject().get("league_id").getAsLong()).ifPresent(match::setLeague);
//                        matchRepository.save(match);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void loadLiveScores()
    {
//        String content = fetchContent(root + "scores/live" + apiKey + secretKey);
//        JsonObject jsonObject = toJsonObject(content);
//        if (jsonObject.get("success").getAsBoolean())
//        {
//            JsonArray jsonLiveScores = jsonObject.get("data").getAsJsonObject().get("match").getAsJsonArray();
//            for (JsonElement element : jsonLiveScores)
//            {
//                LiveScore liveScore = liveScoreRepository.findById(element.getAsJsonObject().get("id").getAsLong()).orElseGet(LiveScore::new);
//                liveScore.setId(element.getAsJsonObject().get("id").getAsLong());
//                liveScore.setHomeId(element.getAsJsonObject().get("home_id").getAsLong());
//                liveScore.setHomeName(element.getAsJsonObject().get("home_name").getAsString());
//                liveScore.setAwayId(element.getAsJsonObject().get("away_id").getAsLong());
//                liveScore.setAwayName(element.getAsJsonObject().get("away_name").getAsString());
//                liveScore.setScore(element.getAsJsonObject().get("score").getAsString());
//                liveScore.setHtScore(element.getAsJsonObject().get("ht_score").getAsString());
//                liveScore.setFtScore(element.getAsJsonObject().get("ft_score").getAsString());
//                liveScore.setEtScore(element.getAsJsonObject().get("et_score").getAsString());
//                liveScore.setTime(element.getAsJsonObject().get("time").getAsString());
//                liveScore.setStatus(element.getAsJsonObject().get("status").getAsString());
//                liveScore.setAdded(element.getAsJsonObject().get("added").getAsString());
//                liveScore.setLastChanged(element.getAsJsonObject().get("last_changed").getAsString());
//                liveScore.setEvents(element.getAsJsonObject().get("events").getAsBoolean());
//                leagueRepository.findById(element.getAsJsonObject().get("league_id").getAsLong()).ifPresent(liveScore::setLeague);
//                liveScoreRepository.save(liveScore);
//            }
//        }
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
