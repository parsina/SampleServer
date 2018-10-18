package com.coin.app.service.background;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.coin.app.model.Winner;
import com.coin.app.model.enums.FixtureStatus;
import com.coin.app.model.enums.FormStatus;
import com.coin.app.model.enums.FormTemplateStatus;
import com.coin.app.model.enums.FormTemplateType;
import com.coin.app.model.enums.WinnerPlace;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.livescore.Match;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.FormTemplateRepository;
import com.coin.app.repository.MatchRepository;
import com.coin.app.repository.WinnerRepository;
import com.coin.app.service.BitcoinJService;
import com.coin.app.service.LiveScoreService;
import com.coin.app.util.Utills;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private BitcoinJService bitcoinJService;

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

        //Update Wallet Transaction Status
        bitcoinJService.updateWalletJob();

        //Update winners
        List<FormTemplateStatus> formTemplateStatuses = new ArrayList<>();
        formTemplateStatuses.add(FormTemplateStatus.PASSED);

        for (FormTemplate formTemplate : formTemplateRepository.findAllByStatusIsInOrderByCreatedDateAsc(formTemplateStatuses))
            if (winnerRepository.countByFormFormTemplate(formTemplate) == 0)
                findWinners(formTemplate);


        // Updates fixture data every 1 min to detect the changes in used fixtures and save data to push them to users
//        for (Fixture fixture : fixtureRepository.findByUsedAndLocalDateEqualsAndStatusIsNotInAndFormTemplateStatusIsInOrderByDateAscTimeAsc(true, LocalDate.now(), fixtureStatuses, formTemplateStatuses))
        formTemplateStatuses = new ArrayList<>();
        formTemplateStatuses.add(FormTemplateStatus.OPEN);
        formTemplateStatuses.add(FormTemplateStatus.CLOSE);

        List<FixtureStatus> fixtureStatuses = new ArrayList<>();
        fixtureStatuses.add(FixtureStatus.FT);
        fixtureStatuses.add(FixtureStatus.CANCEL);

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
                    try
                    {
                        createPhotoCalPDF(formTemplate.getId());
                        formTemplate.setStatus(FormTemplateStatus.CLOSE);
                        formTemplateRepository.save(formTemplate);
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    } catch (DocumentException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Updates forms and formTemplates scores and status based on matches and fixture data

        List<FormTemplate> formTemplateList = new ArrayList<>();
        for (Form form : formRepository.findByStatus(FormStatus.FINALIZED))
            if (!formTemplateList.contains(form.getFormTemplate()))
                formTemplateList.add(form.getFormTemplate());
        for (FormTemplate formTemplate : formTemplateList)
        {
            long totalValue = 0;
            int formCount = 0;
            List<Form> forms = formRepository.findByFormTemplate(formTemplate);
            for (Form form : forms)
            {
                boolean allMatchesInFormAreDone = true;
                int formScore = 0;
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
                    formTemplate.setStatus(FormTemplateStatus.PASSED);
                }
                formRepository.save(form);
            }

            formTemplate.setNumberOfForms(formCount);
            formTemplate.setTotalValue(totalValue);
            formTemplateRepository.save(formTemplate);
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

//        liveScoreService.loadFixtureBooks();
//        System.out.println("\n------------------------------------------");
//        System.out.println("------------------------------------------");
//        System.out.println(" >>>>>>>>  Daily Jobs : Load Fixture Books ==> " + LocalDate.now());
//        System.out.println("------------------------------------------");
//        System.out.println("------------------------------------------\n");
    }

    private void findWinners(FormTemplate formTemplate)
    {
        int place = 0;
        for (int score = 15; score > 0; score--)
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
                }

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
                }

                // Find BRONZE Winners
                if (formTemplate.getType().equals(FormTemplateType.BRONZE))
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

    private void createPhotoCalPDF(Long formTemplateId) throws IOException, DocumentException
    {
        FormTemplate formTemplate = formTemplateRepository.findById(formTemplateId).get();
        String fileName = "PhotoCal_" + formTemplate.getType().name() + "_" + formTemplate.getId() + ".pdf";
        String destination = "D://coin/photoCal/" + fileName;
        String fontPath = "C://Users/javad.farzaneh/projects/Examples/coinServer/src/main/resources/font/Vazir.ttf";
        Font font = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        font.setSize(10);
        Font blueFont = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        blueFont.setColor(BaseColor.BLUE);
        blueFont.setSize(8);

        List<Form> forms = formRepository.findByFormTemplateOrderByCreatedDateAscCreatedTimeAsc(formTemplate);
        /////////////////////////////////////////////////////////////////////////////////
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(destination));
        document.open();


        int formCount = 0;
        for (Form form : forms)
        {
            formCount++;

            // Header Data
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingAfter(10);
            table.setSpacingBefore(100);
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            Phrase phrase = new Phrase("");
            Chunk chunk1 = new Chunk("کاربر: ", font);
            Chunk chunk2 = new Chunk(form.getAccount().getUser().getUsername(), blueFont);
            phrase.add(chunk1);
            phrase.add(chunk2);
            PdfPCell cell = new PdfPCell(phrase);
            cell.setBorder(0);
            table.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("فرم: ", font);
            chunk2 = new Chunk(form.getName(), blueFont);
            phrase.add(chunk1);
            phrase.add(chunk2);
            cell = new PdfPCell(phrase);
            cell.setBorder(0);
            table.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("تاریخ ثبت: ", font);
            chunk2 = new Chunk(Utills.nameDisplayForDate(form.getCreatedDate(), false) + " (" + Utills.shortDisplayForTime(form.getCreatedTime().toString()) + ")", blueFont);
            phrase.add(chunk1);
            phrase.add(chunk2);
            cell = new PdfPCell(phrase);
            cell.setBorder(0);
            table.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("مبلغ: ", font);
            chunk2 = new Chunk(Utills.commaSeparator(String.valueOf(form.getValue())) + " ساتوشی", blueFont);
            phrase.add(chunk1);
            phrase.add(chunk2);
            cell = new PdfPCell(phrase);
            cell.setBorder(0);
            table.addCell(cell);

            document.add(table);

            LineSeparator separator = new LineSeparator();
            separator.setLineColor(WebColors.getRGBColor("#e5e6e9"));
            Chunk linebreak = new Chunk(separator);
            document.add(linebreak);

            //Form Data
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            PdfPTable formTable = new PdfPTable(new float[]{25, 7, 7, 7, 25, 20, 5});
            formTable.setWidthPercentage(100);
            formTable.setSpacingBefore(10);
            formTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            phrase = new Phrase("");
            chunk1 = new Chunk("ردیف", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.setFixedHeight(20);
            cell.setHorizontalAlignment(1);
            cell.disableBorderSide(4);
            formTable.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("زمان بازی", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.setHorizontalAlignment(1);
            cell.setVerticalAlignment(1);
            cell.disableBorderSide(12);
            formTable.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("تیم میزبان", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.setHorizontalAlignment(1);
            cell.setVerticalAlignment(1);
            cell.disableBorderSide(12);
            formTable.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("میزبان", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.setHorizontalAlignment(1);
            cell.setVerticalAlignment(1);
            cell.disableBorderSide(12);
            formTable.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("مساوی", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.setHorizontalAlignment(1);
            cell.setVerticalAlignment(1);
            cell.disableBorderSide(12);
            formTable.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("میهمان", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.disableBorderSide(12);
            formTable.addCell(cell);

            phrase = new Phrase("");
            chunk1 = new Chunk("تیم میهمان", font);
            phrase.add(chunk1);
            cell = new PdfPCell(phrase);
            cell.setBackgroundColor(WebColors.getRGBColor("#d9effc"));
            cell.setHorizontalAlignment(1);
            cell.setVerticalAlignment(1);
            cell.disableBorderSide(8);
            formTable.addCell(cell);

            document.add(formTable);

            //Match Data
            //////////////////////////////////////////////////////////////////////////////////////////////////////

            PdfPTable matchTable = new PdfPTable(new float[]{25, 7, 7, 7, 25, 20, 5});
            matchTable.setWidthPercentage(100);
            matchTable.setSpacingBefore(1);
            matchTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            int count = 0;
            for (Match match : matchRepository.findByForm(form))
            {
                Fixture fixture = fixtureRepository.findById(match.getFixtureId()).get();

                count++;

                // Counter
                phrase = new Phrase("");
                chunk1 = new Chunk(String.valueOf(count), font);
                phrase.add(chunk1);
                cell = new PdfPCell(phrase);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setFixedHeight(20);
                cell.setHorizontalAlignment(1);
                cell.disableBorderSide(4);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);

                // Date and Time
                phrase = new Phrase("");
                chunk1 = new Chunk(Utills.nameDisplayForDate(fixture.getLocalDate(), false) + " " + Utills.shortDisplayForTime(fixture.getTime()), font);
                phrase.add(chunk1);
                cell = new PdfPCell(phrase);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setHorizontalAlignment(1);
                cell.setVerticalAlignment(1);
                cell.disableBorderSide(12);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);

                // Local Team
                phrase = new Phrase("");
                chunk1 = new Chunk(fixture.getLocalTeamName(), font);
                phrase.add(chunk1);
                cell = new PdfPCell(phrase);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setHorizontalAlignment(1);
                cell.setVerticalAlignment(1);
                cell.disableBorderSide(12);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);


                Image checked_1 = Image.getInstance("C:\\Users\\javad.farzaneh\\projects\\Examples\\coinServer\\src\\main\\resources\\image\\checked_1.png");
                Image unchecked_1 = Image.getInstance("C:\\Users\\javad.farzaneh\\projects\\Examples\\coinServer\\src\\main\\resources\\image\\unchecked_1.png");

                Image checked_2 = Image.getInstance("C:\\Users\\javad.farzaneh\\projects\\Examples\\coinServer\\src\\main\\resources\\image\\checked_2.png");
                Image unchecked_2 = Image.getInstance("C:\\Users\\javad.farzaneh\\projects\\Examples\\coinServer\\src\\main\\resources\\image\\unchecked_2.png");

                // Local Win
                cell = new PdfPCell(match.isLocalWin() ? (count % 2 == 1 ? checked_1 : checked_2) : (count % 2 == 1 ? unchecked_1 : unchecked_2), true);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setHorizontalAlignment(1);
                cell.setVerticalAlignment(1);
                cell.disableBorderSide(12);
                cell.setFixedHeight(12);
                cell.setPadding(2);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);

                // No Win
                cell = new PdfPCell(match.isNoWin() ? (count % 2 == 1 ? checked_1 : checked_2) : (count % 2 == 1 ? unchecked_1 : unchecked_2), true);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setHorizontalAlignment(1);
                cell.setVerticalAlignment(1);
                cell.disableBorderSide(12);
                cell.setFixedHeight(12);
                cell.setPadding(2);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);

                // Visitor Win
                cell = new PdfPCell(match.isVisitorWin() ? (count % 2 == 1 ? checked_1 : checked_2) : (count % 2 == 1 ? unchecked_1 : unchecked_2), true);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setHorizontalAlignment(1);
                cell.setVerticalAlignment(1);
                cell.disableBorderSide(12);
                cell.setFixedHeight(12);
                cell.setPadding(2);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);

                // Visitoe Team
                phrase = new Phrase("");
                chunk1 = new Chunk(fixture.getVisitorTeamName(), font);
                phrase.add(chunk1);
                cell = new PdfPCell(phrase);
                cell.setBackgroundColor(count % 2 == 1 ? WebColors.getRGBColor("#e5e6e9") : BaseColor.WHITE);
                cell.setHorizontalAlignment(1);
                cell.setVerticalAlignment(1);
                cell.disableBorderSide(8);
                cell.setBorderWidthTop(0);
                matchTable.addCell(cell);
            }

            document.add(matchTable);

            if (formCount % 2 == 0)
                document.newPage();
        }

        document.close();
    }
}
