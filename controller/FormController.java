package com.coin.app.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.FormService;
import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/form")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
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

    @Secured("ROLE_ADMIN")
    @PostMapping("/createFormTemplate")
    public List<ResultData> createFormTemplate(@RequestBody Map<String, List<Long>> input)
    {
        return formService.createFormTemplate(input.get("ids"));
    }

    @GetMapping("/formTemplates")
    public List<ResultData> formTemplates()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
        System.out.println(">>>>> Update Time : " + new Date());
        return notifier;
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/createForm")
    public ResultData createForm(@RequestBody Map<String, Object> input)
    {
        return formService.createForm(Long.valueOf(input.get("formTemplateId").toString()), Long.valueOf(input.get("userId").toString()),  (List<ResultData>) input.get("formData"));
    }

}
