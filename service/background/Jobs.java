package com.coin.app.service.background;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimerTask;

import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.FormTemplateStatus;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Jobs extends TimerTask
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Override
    public void run()
    {
        StringBuilder fixtureIds = new StringBuilder();
        int i = 0;
        for(Fixture fixture : fixtureRepository.findByUsedAndFormTemplateStatusAndLocalDateEqualsOrderByDateAscTimeAsc(true, FormTemplateStatus.OPEN, LocalDate.now(ZoneId.of("Asia/Tehran"))))
        {
            i++;
            fixtureIds.append(fixture.getId()).append(",");
            if(i == 10 )
            {
                i = 0;
                liveScoreService.updateFixtureData(fixtureIds.substring(0, fixtureIds.length() - 1));
            }
        }
    }
}
