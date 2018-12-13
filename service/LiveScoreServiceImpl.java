package com.coin.app.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.FixtureInfo;
import com.coin.app.model.enums.FixtureInfoType;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.enums.FixtureStatus;
import com.coin.app.repository.BookDataRepository;
import com.coin.app.repository.BookRepository;
import com.coin.app.repository.FixtureInfoRepository;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.util.Utills;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ir.huri.jcal.JalaliCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookDataRepository bookDataRepository;

    @Autowired
    private FixtureInfoRepository fixtureInfoRepository;

    private Properties props = new Properties();

    @Async
    @Override
    public void loadFixtures()
    {
        String fDate = LocalDate.now().minusDays(1).toString();
        String tDate = LocalDate.now().plusDays(20).toString();

        String uri = this.root + "fixtures/between/" + fDate + "/" + tDate + "?api_token=" + this.key + "&include=localTeam,visitorTeam,league,inplay";
        String content = fetchContent(uri);
        JsonObject fixtureObject = toJsonObject(content);
        if (fixtureObject.get("data") != null)
        {
            JsonArray dataArray = fixtureObject.get("data").getAsJsonArray();
            for (JsonElement element : dataArray)
                updateFixtureData(element.getAsJsonObject());
        }
    }

    @Override
    public List<ResultData> findAllFreeFixtures()
    {
        List<ResultData> results = new ArrayList<>();

        List<Fixture> fixtures = fixtureRepository.findTop50ByUsedAndStatusIsAndLocalDateGreaterThanOrderByDateAscTimeAsc(false, FixtureStatus.NS, LocalDate.now().plusDays(4));

        for (Fixture fixture : fixtures)
        {
            ResultData result = new ResultData(true, "");
            result.addProperty("id", fixture.getId());
            result.addProperty("checked", false);
            result.addProperty("date", Utills.nameDisplayForDate(fixture.getDate(), true));
            result.addProperty("time", Utills.shortDisplayForTime(fixture.getTime()));
            result.addProperty("league", fixtureInfoRepository.findByName(fixture.getLeagueName()).getFarsiName());
            result.addProperty("homeTeam", fixtureInfoRepository.findByName(fixture.getLocalTeamName()).getFarsiName());
            result.addProperty("homeLogo", fixture.getLocalTeamLogo());
            result.addProperty("homeCountry", fixtureInfoRepository.findByName(fixture.getLocalCountryName()).getFarsiName());
            result.addProperty("awayTeam", fixtureInfoRepository.findByName(fixture.getVisitorTeamName()).getFarsiName());
            result.addProperty("awayLogo", fixture.getVisitorTeamLogo());
            result.addProperty("awayCountry", fixtureInfoRepository.findByName(fixture.getVisitorCountryName()).getFarsiName());
            results.add(result);
        }
        return results;
    }

    private void updateFixtureData(JsonObject fixtureObject)
    {
        Fixture fixture = fixtureRepository.findById(fixtureObject.get("id").getAsLong()).isPresent() ?
                fixtureRepository.findById(fixtureObject.get("id").getAsLong()).get() :
                new Fixture();
        if (fixture.getId() == null)
            fixture.setId(fixtureObject.get("id").getAsLong());

        // General Data
        String date_time = fixtureObject.get("time").getAsJsonObject().get("starting_at").getAsJsonObject().get("date_time").getAsString();
        fixture.setLocalDate(LocalDate.parse(date_time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String date = fixtureObject.get("time").getAsJsonObject().get("starting_at").getAsJsonObject().get("date").getAsString();
        fixture.setDate(new JalaliCalendar(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toString());
        fixture.setTime(fixtureObject.get("time").getAsJsonObject().get("starting_at").getAsJsonObject().get("time").getAsString());
        fixture.setLeagueId(fixtureObject.get("league_id").getAsLong());
        fixture.setLeagueName(fixtureObject.get("league").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString());
        fixture.setStatus(findFixtureStatus(fixtureObject.get("time").getAsJsonObject().get("status").isJsonNull() ? null : fixtureObject.get("time").getAsJsonObject().get("status").getAsString()));
        fixture.setMinute(fixtureObject.get("time").getAsJsonObject().get("minute").isJsonNull() ? null : fixtureObject.get("time").getAsJsonObject().get("minute").getAsString());
        fixture.setExtraTime(fixtureObject.get("time").getAsJsonObject().get("extra_minute").isJsonNull() ? null : fixtureObject.get("time").getAsJsonObject().get("extra_minute").getAsString());
        fixture.setAddedTime(fixtureObject.get("time").getAsJsonObject().get("added_time").isJsonNull() ? null : fixtureObject.get("time").getAsJsonObject().get("added_time").getAsString());
        fixture.setHalfTimeScore(fixtureObject.get("scores").getAsJsonObject().get("ht_score").isJsonNull() ? null : fixtureObject.get("scores").getAsJsonObject().get("ht_score").getAsString());
        fixture.setFullTimeScore(fixtureObject.get("scores").getAsJsonObject().get("ft_score").isJsonNull() ? null : fixtureObject.get("scores").getAsJsonObject().get("ft_score").getAsString());
        fixture.setExtraTimeScore(fixtureObject.get("scores").getAsJsonObject().get("et_score").isJsonNull() ? null : fixtureObject.get("scores").getAsJsonObject().get("et_score").getAsString());

        // Local Team Data
        fixture.setLocalTeamId(fixtureObject.get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("id").getAsLong());
        fixture.setLocalTeamCountryId(fixtureObject.get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("country_id").getAsLong());
        fixture.setLocalTeamScore(fixtureObject.get("scores").getAsJsonObject().get("localteam_score").getAsInt());
        fixture.setLocalTeamName(fixtureObject.get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString());
        fixture.setLocalTeamLogo(fixtureObject.get("localTeam").getAsJsonObject().get("data").getAsJsonObject().get("logo_path").getAsString());
        String countryURI = this.root + "countries/" + fixture.getLocalTeamCountryId() + "?api_token=" + this.key;
        String countryContent = fetchContent(countryURI);
        if (countryContent != null)
        {
            JsonObject countryObject = toJsonObject(countryContent);
            fixture.setLocalCountryName(countryObject.get("data").getAsJsonObject().get("name").getAsString().equals("Monaco") ? "France" : countryObject.get("data").getAsJsonObject().get("name").getAsString());
            if (!countryObject.get("data").getAsJsonObject().get("extra").toString().equals("null"))
            {
                fixture.setLocalCountryFIFAName(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("fifa").getAsString());
                fixture.setLocalCountryFlag(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("flag").getAsString());
            }
        }

        // Visitor Team Data
        fixture.setVisitorTeamId(fixtureObject.get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("id").getAsLong());
        fixture.setVisitorTeamCountryId(fixtureObject.get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("country_id").getAsLong());
        fixture.setVisitorTeamScore(fixtureObject.get("scores").getAsJsonObject().get("visitorteam_score").getAsInt());
        fixture.setVisitorTeamName(fixtureObject.get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString());
        fixture.setVisitorTeamLogo(fixtureObject.get("visitorTeam").getAsJsonObject().get("data").getAsJsonObject().get("logo_path").getAsString());
        countryURI = this.root + "countries/" + fixture.getVisitorTeamCountryId() + "?api_token=" + this.key;
        countryContent = fetchContent(countryURI);
        if (countryContent != null)
        {
            JsonObject countryObject = toJsonObject(countryContent);
            fixture.setVisitorCountryName(countryObject.get("data").getAsJsonObject().get("name").getAsString().equals("Monaco") ? "France" : countryObject.get("data").getAsJsonObject().get("name").getAsString());
            if (!countryObject.get("data").getAsJsonObject().get("extra").toString().equals("null"))
            {
                fixture.setVisitorCountryFIFAName(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("fifa").getAsString());
                fixture.setVisitorCountryFlag(countryObject.get("data").getAsJsonObject().get("extra").getAsJsonObject().get("flag").getAsString());
            }
        }

        fixtureRepository.save(fixture);
    }

    @Override
    public List<ResultData> getCountries(boolean onlyWithFarsiName)
    {
        List<ResultData> countriesData = new ArrayList<>();
        List<String> countries = new ArrayList<>();
        countries.addAll(fixtureRepository.findLocalCountries());
        for (String country : fixtureRepository.findVisitorCountries())
            if (!countries.contains(country))
                countries.add(country);

        for (String country : countries)
        {
            ResultData resultData = new ResultData(true, "");
            FixtureInfo fixtureInfo = fixtureInfoRepository.findByTypeAndName(FixtureInfoType.COUNTRY, country);
            resultData.addProperty("name", country);
            resultData.addProperty("farsiName", fixtureInfo == null ? "" : fixtureInfo.getFarsiName());
            countriesData.add(resultData);
        }
        return countriesData;
    }

    @Override
    public void saveCountry(String key, String value)
    {
        FixtureInfo fixtureInfo = fixtureInfoRepository.findByTypeAndName(FixtureInfoType.COUNTRY, key);
        if (fixtureInfo == null)
            fixtureInfo = new FixtureInfo(FixtureInfoType.COUNTRY, key, value);
        fixtureInfo.setFarsiName(value);
        fixtureInfoRepository.save(fixtureInfo);
    }

    @Override
    public List<ResultData> getLeagues(boolean onlyWithFarsiName)
    {
        List<ResultData> leaguesData = new ArrayList<>();
        List<String> leagues = new ArrayList<>();
        leagues.addAll(fixtureRepository.findLeages());

        for (String league : leagues)
        {
            ResultData resultData = new ResultData(true, "");
            FixtureInfo fixtureInfo = fixtureInfoRepository.findByTypeAndName(FixtureInfoType.LEAGUE, league);
            resultData.addProperty("name", league);
            resultData.addProperty("farsiName", fixtureInfo == null ? "" : fixtureInfo.getFarsiName());
            leaguesData.add(resultData);
        }
        return leaguesData;
    }

    @Override
    public void saveLeague(String key, String value)
    {
        FixtureInfo fixtureInfo = fixtureInfoRepository.findByTypeAndName(FixtureInfoType.LEAGUE, key);
        if (fixtureInfo == null)
            fixtureInfo = new FixtureInfo(FixtureInfoType.LEAGUE, key, value);
        fixtureInfo.setFarsiName(value);
        fixtureInfoRepository.save(fixtureInfo);
    }

    @Override
    public List<ResultData> getTeams(boolean onlyWithFarsiName)
    {

        List<ResultData> teamsData = new ArrayList<>();
        List<String> teams = new ArrayList<>();
        teams.addAll(fixtureRepository.findLocalTeams());
        for (String team : fixtureRepository.findVisitorTeams())
            if (!teams.contains(team))
                teams.add(team);

        for (String team : teams)
        {
            ResultData resultData = new ResultData(true, "");
            FixtureInfo fixtureInfo = fixtureInfoRepository.findByTypeAndName(FixtureInfoType.TEAM, team);
            resultData.addProperty("name", team);
            resultData.addProperty("farsiName", fixtureInfo == null ? "" : fixtureInfo.getFarsiName());
            teamsData.add(resultData);
        }
        return teamsData;
    }

    @Override
    public void saveTeam(String key, String value)
    {
        FixtureInfo fixtureInfo = fixtureInfoRepository.findByTypeAndName(FixtureInfoType.TEAM, key);
        if (fixtureInfo == null)
            fixtureInfo = new FixtureInfo(FixtureInfoType.TEAM, key, value);
        fixtureInfo.setFarsiName(value);
        fixtureInfoRepository.save(fixtureInfo);
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

    private FixtureStatus findFixtureStatus(String status)
    {
        switch (status)
        {
            case "NS":
                return FixtureStatus.NS;

            case "POSTP":
            case "INT":
            case "ABAN":
            case "SUSP":
            case "AWARDED":
            case "TBA":
            case "WO":
            case "AU":
            case "CANCEL":
                return FixtureStatus.CANCEL;

            case "DELAYED":
                return FixtureStatus.DELAYED;

            case "DELETED":
                return FixtureStatus.DELETED;

            case "BREAK":
                return FixtureStatus.BREAK;

            case "HT":
                return FixtureStatus.HT;

            case "AET":
            case "FT_PEN":
            case "FT":
                return FixtureStatus.FT;

            default:
                return FixtureStatus.LIVE;
        }
    }
}
