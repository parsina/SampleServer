package com.coin.app.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.FormType;
import com.coin.app.service.FormService;
import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        return formService.findAllFreeMatches(0);
    }

    @GetMapping("/loadMatches")
    public List<ResultData> loadMatches()
    {
        liveScoreService.loadData();
        return formService.findAllFreeMatches(0);
    }

    @PostMapping("/createForm")
    public ResultData createTemplateForm(@RequestBody Map<String, List<Long>> input)
    {
        formService.createForm(input.get("ids"), FormType.TEMPLATE);
        return null;
    }
}
