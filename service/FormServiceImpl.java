package com.coin.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormStatus;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.livescore.FormTemplateStatus;
import com.coin.app.model.livescore.Match;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.FormTemplateRepository;
import com.coin.app.repository.MatchRepository;
import com.coin.app.util.Utills;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl implements FormService
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserService userService;

    @Override
    public ResultData findFormTemplate(Long formId)
    {
        FormTemplate formTemplate = formTemplateRepository.findById(formId).orElse(null);
        if (formTemplate == null)
            return new ResultData(false, "Error in fetching data");
        ResultData resultData = new ResultData(true, "");
        resultData.addProperty("id", formTemplate.getId());
        resultData.addProperty("name", formTemplate.getName());
        resultData.addProperty("value", formTemplate.getTotalValue());
        List<Fixture> fixtures = fixtureRepository.findByFormTemplateOrderByDateAscTimeAsc(formTemplateRepository.findById(formId).get());
        resultData.addProperty("matches", getMatchData(fixtures));
        return resultData;
    }

    @Override
    public List<ResultData> createFormTemplate(List<Long> matchIds)
    {
        FormTemplate formTemplate = formTemplateRepository.save(new FormTemplate(String.valueOf(formTemplateRepository.count() + 1), 10));

        for (Long id : matchIds)
        {
            Fixture fixture = fixtureRepository.findById(id).get();
            fixture.setUsed(true);
            fixture.setFormTemplate(formTemplate);
            fixtureRepository.save(fixture);
        }
        return liveScoreService.findAllFreeFixtures();
    }

    @Override
    public ResultData createForm(Long formTemplateId, Long userId, List<ResultData> matchesData)
    {
        ResultData resultData = new ResultData(false, "");

        if(userService.isAuthenticated(userId))
        {
            int counter = 0;
            for (Object matchData : matchesData)
            {
                Map data = ((Map) ((LinkedHashMap) matchData).get("properties"));
                if(data.get("homeWin") == null || data.get("awayWin") == null || data.get("noWin") == null)
                {
                    resultData.setMessage("Null in row data");
                    return resultData;
                }

                if(!Boolean.valueOf(data.get("homeWin").toString()) && !Boolean.valueOf(data.get("awayWin").toString()) && !Boolean.valueOf(data.get("noWin").toString()))
                {
                    resultData.setMessage("Not selected data for all rows");
                    return resultData;
                }

                if(data.get("homeWin") != null && Boolean.valueOf(data.get("homeWin").toString()))
                    counter++;
                if(data.get("awayWin") != null && Boolean.valueOf(data.get("awayWin").toString()))
                    counter++;
                if(data.get("noWin") != null && Boolean.valueOf(data.get("noWin").toString()))
                    counter++;
            }

            Long value = 100L;
            if(counter > 10)
            {
                for (int i = 0; i < counter - 10; i++)
                    value = value * 2;
            }

            FormTemplate formTemplate = formTemplateRepository.findById(formTemplateId).get();
            Form form = new Form(String.valueOf(formRepository.count() + 1 ), LocalDate.now(ZoneId.of("Asia/Tehran")), LocalTime.now(ZoneId.of("Asia/Tehran")), FormStatus.REGISTERED, formTemplate);
            form.setValue(value);
            form.setAccount(userService.findById(userId).getAccount());
            formRepository.save(form);

            for (Object matchData : matchesData)
            {
                Map data = ((Map)((LinkedHashMap) matchData).get("properties"));
                Match match = new Match();
                match.setFixtureId((Long.valueOf(data.get("id").toString())));
                match.setLocalWin(data.get("homeWin") == null ? false : Boolean.valueOf(data.get("homeWin").toString()));
                match.setVisitorWin(data.get("awayWin") == null ? false : Boolean.valueOf(data.get("awayWin").toString()));
                match.setNoWin(data.get("noWin") == null ? false : Boolean.valueOf(data.get("noWin").toString()));
                match.setScore(false);
                match.setForm(form);
                matchRepository.save(match);
            }

            resultData.setSuccess(true);
            resultData.setMessage("Form is submitted !");
            return resultData;
        }
        else
            resultData.setMessage("User is not allowed to create form !");
        return resultData;
    }

    @Override
    public List<ResultData> findFormTemplates()
    {
        List<ResultData> resultDataList = new ArrayList<>();
        for (FormTemplate formTemplate : formTemplateRepository.findAll())
        {
            ResultData result = new ResultData(true, "");
            result.addProperty("id", formTemplate.getId());
            result.addProperty("name", formTemplate.getName());
            resultDataList.add(result);
        }
        return resultDataList;
    }

    @Override
    public ResultData getUpdatedFixturesData()
    {
        ResultData resultData = new ResultData(true, "");
        resultData.addProperty("matches", getMatchData(fixtureRepository.findByUsedAndFormTemplateStatusOrderByDateAscTimeAsc(true, FormTemplateStatus.OPEN)));
        return resultData;
    }

    private List<ResultData> getMatchData(List<Fixture> fixtures)
    {
        List<ResultData> matchResult = new ArrayList<>();
        for (Fixture fixture : fixtures)
        {
            ResultData matchData = new ResultData(true, "");
            matchData.addProperty("id", fixture.getId());
            matchData.addProperty("formTemplateId", fixture.getFormTemplate().getId());
            matchData.addProperty("homeName", fixture.getLocalTeamName());
            matchData.addProperty("homeCountry", fixture.getLocalCountryName());
            matchData.addProperty("homeCountryFlag", fixture.getLocalCountryFlag());
            matchData.addProperty("homeLogo", fixture.getLocalTeamLogo());
            matchData.addProperty("homeScore", fixture.getLocalTeamScore());
            matchData.addProperty("awayName", fixture.getVisitorTeamName());
            matchData.addProperty("awayCountry", fixture.getVisitorCountryName());
            matchData.addProperty("awayCountryFlag", fixture.getVisitorCountryFlag());
            matchData.addProperty("awayLogo", fixture.getVisitorTeamLogo());
            matchData.addProperty("awayScore", fixture.getVisitorTeamScore());
            matchData.addProperty("league", fixture.getLeagueName());
            matchData.addProperty("time", Utills.shortDisplayForTime(fixture.getTime()));
            matchData.addProperty("date", Utills.nameDisplayForDate(fixture.getDate(), false));
            matchData.addProperty("minute", fixture.getMinute());
            matchData.addProperty("status", fixture.getStatus());
            matchResult.add(matchData);
        }
        return matchResult;
    }

    @Override
    public List<ResultData> createOrUpdateForm(List<ResultData> formData)
    {
//        formRepository.findById()
//        Form form = new Form();
//        form.setName();
        return null;
    }
}
