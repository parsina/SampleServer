package com.coin.app.service.background;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.coin.app.model.Winner;
import com.coin.app.model.enums.FixtureStatus;
import com.coin.app.model.enums.FormStatus;
import com.coin.app.model.enums.FormTemplateType;
import com.coin.app.model.enums.WinnerPlace;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.enums.FormTemplateStatus;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.livescore.Match;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.FormTemplateRepository;
import com.coin.app.repository.MatchRepository;
import com.coin.app.repository.WinnerRepository;
import com.coin.app.service.LiveScoreService;
import com.coin.app.util.Utills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Jobs extends TimerTask
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private WinnerRepository winnerRepository;

    private LocalDate date = LocalDate.now(ZoneId.of("Asia/Tehran")).minusDays(0);

//    @Async
    @Override
    public void run()
    {
        LocalTime time = LocalTime.now(ZoneId.of("Asia/Tehran"));

        System.out.println(" >>>>>>>>  Minute Jobs start: " + LocalTime.now());

        if (!LocalDate.now(ZoneId.of("Asia/Tehran")).equals(date))
        {
            dailyJobs();
            date = LocalDate.now(ZoneId.of("Asia/Tehran"));
        }

        List<FormTemplateStatus> formTemplateStatuses = new ArrayList<>();
        formTemplateStatuses.add(FormTemplateStatus.OPEN);
        formTemplateStatuses.add(FormTemplateStatus.CLOSE);

        List<FixtureStatus> fixtureStatuses = new ArrayList<>();
        fixtureStatuses.add(FixtureStatus.FT);
        fixtureStatuses.add(FixtureStatus.CANCEL);


        // Updates fixture data every 1 min to detect the changes in used fixtures and save data to push them to users
        for (Fixture fixture : fixtureRepository.findByUsedAndLocalDateEqualsAndStatusIsNotInAndFormTemplateStatusIsInOrderByDateAscTimeAsc(true, LocalDate.now(), fixtureStatuses, formTemplateStatuses))
        {
            if (!fixture.getStatus().equals(FixtureStatus.FT) && !fixture.getStatus().equals(FixtureStatus.CANCEL))
                liveScoreService.updateFixtureData(fixture.getId().toString());
            if (time.getHour() >= LocalTime.parse(fixture.getTime()).minusHours(1).getHour() && time.getMinute() >= LocalTime.parse(fixture.getTime()).minusHours(1).getMinute())
            {
                FormTemplate formTemplate = formTemplateRepository.findById(fixture.getFormTemplate().getId()).get();
                for (Form form : formRepository.findByFormTemplate(formTemplate))
                {
                    if (form.getStatus().equals(FormStatus.REGISTERED))
                    {
                        form.setStatus(FormStatus.FINALIZED);
                        formRepository.save(form);
                    }
                }
                if (formTemplate.getStatus().equals(FormTemplateStatus.OPEN))
                {
                    formTemplate.setStatus(FormTemplateStatus.CLOSE);
                    formTemplateRepository.save(formTemplate);
                }
            }
        }

        for (Form form : formRepository.findAll())
            if (winnerRepository.countByFormFormTemplate(form.getFormTemplate()) == 0)
                findWinners(form.getFormTemplate());

        // Updates forms and formTemplates scores and status based on matches and fixture data
        for (Form form : formRepository.findByStatus(FormStatus.FINALIZED))
        {
            boolean allMatchesInFormAreDone = true;
            long totalValue = 0;
            int formScore = 0;
            int formCount = 0;
            List<Match> matches = matchRepository.findByForm(form);
            for (Match match : matches)
            {
                Fixture fixture = fixtureRepository.findById(match.getFixtureId()).get();
                if (fixture.getStatus().equals(FixtureStatus.FT))
                {
                    if (fixture.getLocalTeamScore() > fixture.getVisitorTeamScore() && match.isLocalWin())
                        match.setScore(true);
                    else if (fixture.getLocalTeamScore() == fixture.getVisitorTeamScore() && match.isNoWin())
                        match.setScore(true);
                    else if (fixture.getLocalTeamScore() < fixture.getVisitorTeamScore() && match.isVisitorWin())
                        match.setScore(true);
                } else
                    allMatchesInFormAreDone = false;
                formScore += (match.isScore() ? 1 : 0);
                matchRepository.save(match);
            }
            totalValue += form.getValue();
            form.setScore(formScore);
            formCount++;
            if (allMatchesInFormAreDone)
            {
                form.setStatus(FormStatus.PASSED);
                form.getFormTemplate().setStatus(FormTemplateStatus.PASSED);
                form.getFormTemplate().setTotalValue(totalValue);
                if (winnerRepository.countByFormFormTemplate(form.getFormTemplate()) == 0)
                    findWinners(form.getFormTemplate());
                formTemplateRepository.save(form.getFormTemplate());
            }

            if (form.getFormTemplate().getTotalValue() == 0 && form.getFormTemplate().getStatus().equals(FormTemplateStatus.CLOSE))
            {
                form.getFormTemplate().setNumberOfForms(formCount);
                form.getFormTemplate().setTotalValue(totalValue);
                formTemplateRepository.save(form.getFormTemplate());
            }
            formRepository.save(form);
        }

        System.out.println(" >>>>>>>>  Minute Jobs ends: " + LocalTime.now() + "\n");
    }

    private void dailyJobs()
    {
        liveScoreService.loadFixtures();
        System.out.println("\n------------------------------------------");
        System.out.println("------------------------------------------");
        System.out.println(" >>>>>>>>  Daily Jobs : Load Fixtures ==> " + LocalDate.now());
        System.out.println("------------------------------------------");
        System.out.println("------------------------------------------\n");

        liveScoreService.loadFixtureBooks();
        System.out.println("\n------------------------------------------");
        System.out.println("------------------------------------------");
        System.out.println(" >>>>>>>>  Daily Jobs : Load Fixture Books ==> " + LocalDate.now());
        System.out.println("------------------------------------------");
        System.out.println("------------------------------------------\n");
    }

    private void findWinners(FormTemplate formTemplate)
    {
        if (formTemplate.getStatus().equals(FormTemplateStatus.PASSED))
        {
            int place = 0;
            for (int score = 10; score > 0; score--)
            {
                List<Form> forms = formRepository.findByFormTemplateAndScore(formTemplate, score);
                if (forms.size() > 0)
                {
                    place++;

                    // Find Gold Winners
                    if (formTemplate.getType().equals(FormTemplateType.GOLD))
                    {
                        for (Form form : forms)
                        {
                            Winner winner = new Winner();
                            winner.setForm(form);
                            winner.setPrize(formTemplate.getTotalValue() / forms.size());
                            winner.setWinnerPlace(WinnerPlace.First);
                            winnerRepository.save(winner);
                        }
                        return;
                    } else if (formTemplate.getType().equals(FormTemplateType.SILVER))
                    {
                        // Find Silver Winners
                        if (formTemplate.getType().equals(FormTemplateType.SILVER))
                        {
                            for (Form form : forms)
                            {
                                Winner winner = new Winner();
                                winner.setForm(form);
                                if (place == 1)
                                {
                                    winner.setPrize((75 * formTemplate.getTotalValue()) / (100 * forms.size()));
                                    winner.setWinnerPlace(WinnerPlace.First);
                                } else if (place == 2)
                                {
                                    winner.setPrize((25 * formTemplate.getTotalValue()) / (100 * forms.size()));
                                    winner.setWinnerPlace(WinnerPlace.Second);
                                }
                            winnerRepository.save(winner);
                            }
                            if (place == 2)
                                return;
                        } else if (formTemplate.getType().equals(FormTemplateType.BRONZE))
                        {
                            for (Form form : forms)
                            {
                                Winner winner = new Winner();
                                winner.setForm(form);
                                if (place == 1)
                                {
                                    winner.setPrize((65 * formTemplate.getTotalValue()) / (100 * forms.size()));
                                    winner.setWinnerPlace(WinnerPlace.First);
                                } else if (place == 2)
                                {
                                    winner.setPrize((20 * formTemplate.getTotalValue()) / (100 * forms.size()));
                                    winner.setWinnerPlace(WinnerPlace.Second);
                                } else if (place == 3)
                                {
                                    winner.setPrize((15 * formTemplate.getTotalValue()) / (100 * forms.size()));
                                    winner.setWinnerPlace(WinnerPlace.Third);
                                }
                            winnerRepository.save(winner);
                            }
                            if (place == 3)
                                return;
                        }
                    }
                }
            }
        }
    }
}
