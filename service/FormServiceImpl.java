package com.coin.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormType;
import com.coin.app.model.livescore.Match;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.MatchRepository;
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
    public Form createForm(List<Long> matchIds, FormType type)
    {
        Form form = new Form("مسابقه شماره " + formRepository.count(), type);
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
}
