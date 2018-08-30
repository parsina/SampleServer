package com.coin.app.service.background;

import java.util.Date;
import java.util.TimerTask;

import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Jobs extends TimerTask
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Override
    public void run()
    {
        liveScoreService.loadLiveScores();
        System.out.println(" >>>>>>        Jobs : " + new Date());
    }
}
