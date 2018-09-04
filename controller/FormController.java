package com.coin.app.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.FormService;
import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class FormController
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FormService formService;

    @GetMapping("/matchData")
    public List<ResultData> matchData()
    {
        return liveScoreService.findAllFreeFixtures();
    }

    @PostMapping("/createFormTemplate")
    public List<ResultData> createFormTemplate(@RequestBody Map<String, List<Long>> input)
    {
        return formService.createFormTemplate(input.get("ids"));
    }

    @GetMapping("/formTemplates")
    public List<ResultData> formTemplates()
    {
        return formService.findFormTemplates();
    }

    @GetMapping("/formTemplateData")
    public ResultData formTemplateData(Long id)
    {
        return formService.findFormTemplate(id);
    }

    @GetMapping("/updateFormTemplate")
    public SseEmitter updateFTData(Long id)
    {
        ResultData data = formService.getUpdatedFixturesData();
        SseEmitter notifier = new SseEmitter(60000L);
        try
        {
            notifier.send(SseEmitter.event().reconnectTime(500).data(data));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return notifier;
    }

}
