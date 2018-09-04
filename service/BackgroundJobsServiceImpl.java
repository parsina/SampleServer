package com.coin.app.service;

import java.util.Timer;

import com.coin.app.service.background.DailyJobs;
import com.coin.app.service.background.Jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackgroundJobsServiceImpl implements BackgroundJobsService
{
    @Autowired
    private DailyJobs dailyJobs;

    @Autowired
    private Jobs Jobs;

    @Autowired
    private LiveScoreService liveScoreService;

    public void initialize()
    {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(Jobs, 0, 1000L * 60); // 1 Min
        timer.scheduleAtFixedRate(dailyJobs, 0, 1000L * 60 * 60 * 60 * 24); // 1 Day

    }
}

