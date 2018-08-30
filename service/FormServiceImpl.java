package com.coin.app.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.rmi.CORBA.Util;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.repository.FormTemplateRepository;
import com.coin.app.util.Utills;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Service
public class FormServiceImpl implements FormService
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private FixtureRepository fixtureRepository;

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
        JsonArray liveScoresArr = liveScoreService.getLiveScores();
        List<ResultData> matchResult = new ArrayList<>();
        List<Fixture> fixtures = fixtureRepository.findByFormTemplateOrderByDateAscTimeAsc(formTemplateRepository.findById(formId).get());

        for (Fixture fixture : fixtures)
        {
            ResultData matchData = new ResultData(true, "");
            matchData.addProperty("id", fixture.getId());
            matchData.addProperty("homeName", fixture.getLocalTeamName());
            matchData.addProperty("homeCountry", fixture.getLocalCountryName());
            matchData.addProperty("homeCountryFlag", fixture.getLocalCountryFlag());
            matchData.addProperty("homeLogo", fixture.getLocalTeamLogo());
            matchData.addProperty("awayName", fixture.getVisitorTeamName());
            matchData.addProperty("awayCountry", fixture.getVisitorCountryName());
            matchData.addProperty("awayCountryFlag", fixture.getVisitorCountryFlag());
            matchData.addProperty("awayLogo", fixture.getVisitorTeamLogo());
            matchData.addProperty("league", fixture.getLeagueName());
            matchData.addProperty("time", Utills.shortDisplayForTime(fixture.getTime()));
            matchData.addProperty("date", Utills.nameDisplayForDate(fixture.getDate(), false));
//            matchData.addProperty("score", fixture.getScore());
//            for (JsonElement element : liveScoresArr)
//                if (element.getAsJsonObject().get("id").getAsLong() == fixture.getId())
//                {
//                    matchData.addProperty("liveScore", element.getAsJsonObject().get("score"));
//                    matchData.addProperty("liveFTScore", element.getAsJsonObject().get("ft_score"));
//                    matchData.addProperty("liveSTScore", element.getAsJsonObject().get("st_score"));
//                    matchData.addProperty("liveTime", element.getAsJsonObject().get("time"));
//                    matchData.addProperty("liveStatus", element.getAsJsonObject().get("status"));
//                }

            if(!matchData.getProperties().containsKey("liveScore"))
            {
                matchData.addProperty("liveScore", "0 - 0");
                matchData.addProperty("liveFTScore", "0 - 0");
                matchData.addProperty("liveSTScore", "0 - 0");
                matchData.addProperty("liveTime", "00:00");
                matchData.addProperty("liveStatus", "");
            }
            matchResult.add(matchData);
        }
        resultData.addProperty("matches", matchResult);
        return resultData;
    }

    @Override
    public List<ResultData>  createFormTemplate(List<Long> matchIds)
    {
        FormTemplate formTemplate = formTemplateRepository.save(new FormTemplate(String.valueOf(formTemplateRepository.count() + 1), 10 ));

        for(Long id : matchIds)
        {
            Fixture fixture = fixtureRepository.findById(id).get();
            fixture.setUsed(true);
            fixture.setFormTemplate(formTemplate);
            fixtureRepository.save(fixture);
        }
        return liveScoreService.findAllFreeFixtures();
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
    public Flux<ResultData> getFixtureFlux(ResultData resultData)
    {
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(5));
        interval.subscribe(i -> ((List<ResultData>) resultData.getProperties().get("matches")).forEach(result -> result.getProperties().put("liveTime", LocalTime.now().getMinute() + ":" + LocalTime.now().getSecond())));
        Flux<ResultData> resultDataFlux = Flux.fromStream(Stream.generate(() -> resultData));
        return Flux.zip(interval, resultDataFlux).map(Tuple2::getT2);
    }
}
