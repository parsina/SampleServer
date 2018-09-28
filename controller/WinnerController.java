package com.coin.app.controller;

import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.WinnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/winner")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class WinnerController
{
    @Autowired
    private WinnerService winnerService;

    @GetMapping("/winnerListSize")
    public Long winnerListSize(Long formTemplateId)
    {
        return winnerService.countWinnerList(formTemplateId);
    }

    @PostMapping("/winnerList")
    public ResultData winnerList(@RequestBody Map<String, String> input)
    {
        return winnerService.findWinnersByFormTemplate(Long.valueOf(input.get("formTemplateId")), input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }
}
