package com.coin.app.controller;

import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.service.LiveScoreService;
import com.coin.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm")
public class ConfirmationController
{
    @Autowired
    private UserService userService;

    @Autowired
    private LiveScoreService liveScoreService;

    @PostMapping("/user")
    public ResultData confirmUser(String token)
    {
        return userService.confirmRegistration(token);
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/countries")
    public List<ResultData> countries()
    {
        return liveScoreService.getCountries(false);
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/saveCountry")
    public void saveCountry(@RequestBody Map<String, String> input)
    {
        liveScoreService.saveCountry(input.get("key"), input.get("value"));
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/leagues")
    public List<ResultData> leagues()
    {
        return liveScoreService.getLeagues(false);
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/saveLeague")
    public void saveLeague(@RequestBody Map<String, String> input)
    {
        liveScoreService.saveLeague(input.get("key"), input.get("value"));
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/teams")
    public List<ResultData> teams()
    {
        return liveScoreService.getTeams(false);
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/saveTeam")
    public void saveTeam(@RequestBody Map<String, String> input)
    {
        liveScoreService.saveTeam(input.get("key"), input.get("value"));
    }
}
