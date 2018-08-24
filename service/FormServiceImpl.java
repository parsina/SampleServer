package com.coin.app.service;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.xml.bind.Element;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormType;
import com.coin.app.model.livescore.Match;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.MatchRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ir.huri.jcal.JalaliCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl implements FormService
{
    @Autowired
    private FormRepository formRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private LiveScoreService liveScoreService;

    @Override
    public ResultData findForm(Long formId)
    {
        Form form = formRepository.findById(formId).orElse(null);
        if (form == null)
            return new ResultData(false, "Error in fetching data");
        ResultData resultData = new ResultData(true, "");
        resultData.addProperty("id", form.getId());
        resultData.addProperty("name", form.getName());
        resultData.addProperty("value", form.getValue());
        resultData.addProperty("number", form.getNumber());
        resultData.addProperty("score", form.getScore());
        resultData.addProperty("type", form.getType());
        JsonArray liveScoresArr = liveScoreService.getLiveScores();
        List<ResultData> matchResult = new ArrayList<>();

        JalaliCalendar jalaliCalendar = new JalaliCalendar(new GregorianCalendar(2018, 8, 24, 4, 40));

        for (Match match : form.getMatches())
        {
            ResultData matchData = new ResultData(true, "");
            matchData.addProperty("id", match.getId());
            matchData.addProperty("home", match.getHomeName());
            matchData.addProperty("away", match.getAwayName());
            matchData.addProperty("league", match.getLeague().getName());
            matchData.addProperty("country", match.getLeague().getCountry().getName());
            matchData.addProperty("time", match.getTime());
            matchData.addProperty("date", match.getDate());
            matchData.addProperty("score", match.getScore());
            for (JsonElement element : liveScoresArr)
                if (element.getAsJsonObject().get("id").getAsLong() == match.getId())
                {
                    matchData.addProperty("liveScore", element.getAsJsonObject().get("score"));
                    matchData.addProperty("liveFTScore", element.getAsJsonObject().get("ft_score"));
                    matchData.addProperty("liveSTScore", element.getAsJsonObject().get("st_score"));
                    matchData.addProperty("liveTime", element.getAsJsonObject().get("time"));
                    matchData.addProperty("liveStatus", element.getAsJsonObject().get("status"));
                }

            if(!matchData.getProperties().containsKey("liveScore"))
            {
                matchData.addProperty("liveScore", "0 - 0");
                matchData.addProperty("liveFTScore", "0 - 0");
                matchData.addProperty("liveSTScore", "0 - 0");
                matchData.addProperty("liveTime", "00:00");
                matchData.addProperty("liveStatus", "");
            }

            matchData.addProperty("location", match.getLocation());
            matchData.addProperty("awayGoals", match.getAwayGoals());
            matchData.addProperty("homeGoals", match.getHomeGoals());
            matchData.addProperty("awayWin", match.isAwayWin());
            matchData.addProperty("homeWin", match.isHomeWin());
            matchData.addProperty("noWin", match.isNoWin());
            matchResult.add(matchData);
        }
        resultData.addProperty("matches", matchResult);
        return resultData;
    }

    @Override
    public Form createForm(List<Long> matchIds, FormType type)
    {
        Form form = new Form("Form Template " + formRepository.count(), type);
        for (Long matchId : matchIds)
        {
            Match match = matchRepository.findById(matchId).get();
            if (!match.isIncludedInForm())
            {
                form.getMatches().add(match);
                match.setIncludedInForm(true);
                matchRepository.save(match);
            }

        }

        return formRepository.save(form);
//        return null;
    }

    @Override
    public List<ResultData> findAllFreeMatches(int priority)
    {
        List<ResultData> results = new ArrayList<>();

        List<Match> matches;
        if (priority < 0)
            matches = matchRepository.findAll();
        else
            matches = matchRepository.findAllByLeaguePriority(priority);
        for (Match match : matches)
            if (!match.isIncludedInForm())
            {
                ResultData result = new ResultData();
                result.setProperties(new HashMap<>());
                result.addProperty("id", match.getId());
                result.addProperty("checked", false);
                result.addProperty("date", match.getDate());
                result.addProperty("time", match.getTime());
                result.addProperty("country", match.getLeague().getCountry().getName());
                result.addProperty("league", match.getLeague().getName());
                result.addProperty("home", match.getHomeName());
                result.addProperty("away", match.getAwayName());
                result.addProperty("location", match.getLocation());
                results.add(result);
            }
        return results;
    }

    @Override
    public List<ResultData> findForms(FormType type)
    {
        List<ResultData> resultDataList = new ArrayList<>();
        for (Form form : formRepository.findAllByType(type))
        {
            ResultData result = new ResultData(true, "");
            result.addProperty("id", form.getId());
            result.addProperty("name", form.getName());
            resultDataList.add(result);
        }
        return resultDataList;
    }
}
