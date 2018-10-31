package com.coin.app.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.Transaction;
import com.coin.app.model.Wallet;
import com.coin.app.model.enums.FormTemplateType;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.enums.FormStatus;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.enums.FormTemplateStatus;
import com.coin.app.model.livescore.Match;
import com.coin.app.repository.FixtureRepository;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.FormTemplateRepository;
import com.coin.app.repository.MatchRepository;
import com.coin.app.repository.TransactionRepository;
import com.coin.app.repository.UserRepository;
import com.coin.app.repository.WalletRepository;
import com.coin.app.util.Utills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl implements FormService
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public ResultData findFormTemplate(Long formId)
    {
        FormTemplate formTemplate = formTemplateRepository.findById(formId).orElse(null);
        if (formTemplate == null)
            return new ResultData(false, "Error in fetching data");
        ResultData resultData = new ResultData(true, "");
        resultData.addProperty("id", formTemplate.getId());
        resultData.addProperty("status", formTemplate.getStatus().name());
        resultData.addProperty("name", formTemplate.getName());
        resultData.addProperty("type", formTemplate.getType());
        List<Fixture> fixtures = fixtureRepository.findByFormTemplateOrderByDateAscTimeAsc(formTemplateRepository.findById(formId).get());
        resultData.addProperty("matches", getFixtureData(fixtures));
        return resultData;
    }

    @Override
    public List<ResultData> createFormTemplate(List matchIds, FormTemplateType formTemplateType)
    {
        long count = formTemplateRepository.countByType(formTemplateType) + 1;
        String name = "مسابقه برنزی شماره " + count;
        if (formTemplateType == FormTemplateType.GOLD)
            name = "مسابقه طلایی شماره " + count;
        else if (formTemplateType == FormTemplateType.SILVER)
            name = "مسابقه نقره ای شماره " + count;
        FormTemplate formTemplate = formTemplateRepository.save(new FormTemplate(name, formTemplateType));

        for (Object id : matchIds)
        {
            Fixture fixture = fixtureRepository.findById(Long.valueOf(id.toString())).get();
            fixture.setUsed(true);
            fixture.setFormTemplate(formTemplate);
            fixtureRepository.save(fixture);
        }
        return liveScoreService.findAllFreeFixtures();
    }

    @Override
    public ResultData deleteFormTemplate(Long id)
    {
        ResultData resultData = new ResultData(false, "There is a problem on deleting form template !");
        if (userService.isAuthenticated(userService.getCurrentUser().getId()))
        {
            FormTemplate formTemplate = formTemplateRepository.findById(id).get();
            if (formTemplate.getStatus().equals(FormTemplateStatus.OPEN))
            {
                for (Form form : formRepository.findByFormTemplate(formTemplate))
                {
                    for (Match match : matchRepository.findByForm(form))
                        matchRepository.delete(match);
                    deleteUserForm(form.getId());
                }

                for (Fixture fixture : fixtureRepository.findByFormTemplateOrderByDateAscTimeAsc(formTemplate))
                {
                    fixture.setUsed(false);
                    fixture.setFormTemplate(null);
                    fixtureRepository.save(fixture);
                }

                resultData.setSuccess(true);
                resultData.setMessage("Form Template is Deleted !");

                formTemplateRepository.delete(formTemplate);
            }
        }
        return resultData;
    }

    public ResultData deleteUserForm(Long formId)
    {
        ResultData resultData = new ResultData(false, "There is a problem on deleting form !");

        if (userService.isAuthenticated(userService.getCurrentUser().getId()))
        {
            Form form = formRepository.findById(formId).get();
            if (form.getStatus().equals(FormStatus.REGISTERED))
            {
                matchRepository.deleteAll(matchRepository.findByForm(form));

                Transaction transaction = transactionRepository.findByTxId("FAT-" + form.getId() + "-" + form.getAccount().getId() + "-" + form.getFormTemplate().getId());
                transactionRepository.delete(transaction);
                Wallet wallet = walletRepository.findById(form.getAccount().getWallet().getId()).get();
                if(form.isReal())
                {
                    wallet.setBalance((Long.valueOf(wallet.getBalance()) + form.getValue()) + "");
                    walletRepository.save(wallet);
                }
                formRepository.delete(form);
                resultData.setSuccess(true);
                resultData.setMessage("Form is deleted !");
                resultData.addProperty("accountBalance", wallet.getBalance());
            }
        }

        return resultData;
    }

    @Override
    public ResultData createForm(Long formTemplateId, Long userId, boolean realForm, List<ResultData> matchesData)
    {
        ResultData resultData = new ResultData(false, "");

        if (userService.isAuthenticated(userId))
        {
            int counter = 0;
            for (Object matchData : matchesData)
            {
                Map data = ((Map) ((LinkedHashMap) matchData).get("properties"));
                if (data.get("homeWin") == null || data.get("awayWin") == null || data.get("noWin") == null)
                {
                    resultData.setMessage("Null in row data");
                    return resultData;
                }

                if (!Boolean.valueOf(data.get("homeWin").toString()) && !Boolean.valueOf(data.get("awayWin").toString()) && !Boolean.valueOf(data.get("noWin").toString()))
                {
                    resultData.setMessage("Not selected data for all rows");
                    return resultData;
                }

                if (data.get("homeWin") != null && Boolean.valueOf(data.get("homeWin").toString()))
                    counter++;
                if (data.get("awayWin") != null && Boolean.valueOf(data.get("awayWin").toString()))
                    counter++;
                if (data.get("noWin") != null && Boolean.valueOf(data.get("noWin").toString()))
                    counter++;
            }

            Long value = 100L;
            if (counter > 18)
            {
                for (int i = 0; i < counter - 18; i++)
                    value = value * 2;
            }

            Account account = userService.findById(userId).getAccount();
            if (realForm)
            {
                //Checking account balance
                if (value > Long.valueOf(account.getWallet().getBalance()))
                {
                    resultData.setMessage("Account balance is not enough !");
                    return resultData;
                }

                //Update User Account Wallet
                account.getWallet().setBalance((Long.valueOf(account.getWallet().getBalance()) - value) + "");
                walletRepository.save(account.getWallet());
            }

            long formCounts = formRepository.countByAccount(account) + 1;
            FormTemplate formTemplate = formTemplateRepository.findById(formTemplateId).get();
            String name = "BRZ-" + formTemplateRepository.countByType(formTemplate.getType()) + "-" + formCounts;
            if (formTemplate.getType() == FormTemplateType.GOLD)
                name = "GLD-" + formTemplateRepository.countByType(formTemplate.getType()) + "-" + formCounts;
            else if (formTemplate.getType() == FormTemplateType.SILVER)
                name = "SLV-" + formTemplateRepository.countByType(formTemplate.getType()) + "-" + formCounts;

            Form form = new Form();
            form.setName(name);
            form.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            form.setCreatedTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            form.setStatus(FormStatus.REGISTERED);
            form.setFormTemplate(formTemplate);
            form.setValue(value);
            form.setReal(realForm);
            form.setAccount(account);
            form = formRepository.save(form);

            //Create transaction
            Transaction transaction = new Transaction();
            transaction.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            transaction.setCreatedTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            transaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            transaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setType(TransactionType.COST);
            transaction.setAccount(account);
            transaction.setTotalValue(value);
            transaction.setTxId("FAT-" + form.getId() + "-" + account.getId() + "-" + formTemplate.getId());
            transaction.setDescription(formTemplate.getName() + "-" + formCounts + (realForm ? "" : "-مجازی"));
            transactionRepository.save(transaction);


            for (Object matchData : matchesData)
            {
                Map data = ((Map) ((LinkedHashMap) matchData).get("properties"));
                Match match = new Match();
                match.setFixtureId((Long.valueOf(data.get("id").toString())));
                match.setLocalWin(data.get("homeWin") == null ? false : Boolean.valueOf(data.get("homeWin").toString()));
                match.setVisitorWin(data.get("awayWin") == null ? false : Boolean.valueOf(data.get("awayWin").toString()));
                match.setNoWin(data.get("noWin") == null ? false : Boolean.valueOf(data.get("noWin").toString()));
                match.setScore(false);
                match.setForm(form);
                matchRepository.save(match);
            }

            resultData.setSuccess(true);
            resultData.setMessage("Form is submitted !");
            resultData.addProperty("accountBalance", account.getWallet().getBalance());
        } else
            resultData.setMessage("User is not allowed to create form !");
        return resultData;
    }

    @Override
    public ResultData updateForm(Long formId, Long userId, boolean realForm, List<ResultData> matchesData)
    {
        ResultData resultData = new ResultData(false, "");

        if (userService.isAuthenticated(userId))
        {
            int counter = 0;
            for (Object matchData : matchesData)
            {
                Map data = ((Map) ((LinkedHashMap) matchData).get("properties"));
                if (data.get("homeWin") == null || data.get("awayWin") == null || data.get("noWin") == null)
                {
                    resultData.setMessage("Null in row data");
                    return resultData;
                }

                if (!Boolean.valueOf(data.get("homeWin").toString()) && !Boolean.valueOf(data.get("awayWin").toString()) && !Boolean.valueOf(data.get("noWin").toString()))
                {
                    resultData.setMessage("Not selected data for all rows");
                    return resultData;
                }

                if (data.get("homeWin") != null && Boolean.valueOf(data.get("homeWin").toString()))
                    counter++;
                if (data.get("awayWin") != null && Boolean.valueOf(data.get("awayWin").toString()))
                    counter++;
                if (data.get("noWin") != null && Boolean.valueOf(data.get("noWin").toString()))
                    counter++;
            }

            Long value = 100L;
            if (counter > 18)
            {
                for (int i = 0; i < counter - 18; i++)
                    value = value * 2;
            }


            Form form = formRepository.findById(formId).get();
            Account account = userService.findById(userId).getAccount();

            if(realForm)
            {
                //Checking account balance
                if (value > Long.valueOf(account.getWallet().getBalance()))
                {
                    resultData.setMessage("Account balance is not enough !");
                    return resultData;
                }

                //Update User Account Wallet
//                account.getWallet().setBalance((Long.valueOf(account.getWallet().getBalance()) + form.getValue()) + "");

                account.getWallet().setBalance((Long.valueOf(account.getWallet().getBalance()) - value) + "");
                walletRepository.save(account.getWallet());
            }

            form.setValue(value);
            form.setReal(realForm);
            formRepository.save(form);

            //Update transaction
            Transaction transaction = transactionRepository.findByTxId("FAT-" + form.getId() + "-" + form.getAccount().getId() + "-" + form.getFormTemplate().getId());
            transaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
            transaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
            transaction.setTotalValue(value);
            int descSize = transaction.getDescription().split("-").length;
            if(descSize == 2 && !realForm)
                transaction.setDescription(transaction.getDescription() + "-مجازی");
            else if(descSize == 3 && realForm)
                transaction.setDescription(transaction.getDescription().split("-")[0] + "-"+ transaction.getDescription().split("-")[1]);
            transactionRepository.save(transaction);



            List<Match> matches = matchRepository.findByForm(form);
            for (Object matchData : matchesData)
            {
                Map data = ((Map) ((LinkedHashMap) matchData).get("properties"));
                for (Match match : matches)
                    if (match.getFixtureId().equals(Long.valueOf(data.get("id").toString())))
                    {
                        match.setLocalWin(data.get("homeWin") == null ? false : Boolean.valueOf(data.get("homeWin").toString()));
                        match.setVisitorWin(data.get("awayWin") == null ? false : Boolean.valueOf(data.get("awayWin").toString()));
                        match.setNoWin(data.get("noWin") == null ? false : Boolean.valueOf(data.get("noWin").toString()));
                        matchRepository.save(match);
                    }
            }

            resultData.setSuccess(true);
            resultData.setMessage("Form is submitted !");
            resultData.addProperty("accountBalance", account.getWallet().getBalance());
        } else
            resultData.setMessage("User is not allowed to update form !");
        return resultData;
    }

    @Override
    public List<ResultData> findFormTemplatesByStatus(List<FormTemplateStatus> statuses)
    {
        List<ResultData> resultDataList = new ArrayList<>();
        for (FormTemplate formTemplate : formTemplateRepository.findAllByStatusIsInOrderByCreatedDateAsc(statuses))
        {
            ResultData result = new ResultData(true, "");
            result.addProperty("id", formTemplate.getId());
            result.addProperty("name", formTemplate.getName());
            result.addProperty("type", formTemplate.getType().name());
            resultDataList.add(result);
        }
        return resultDataList;
    }

    @Override
    public List<ResultData> findFormTemplatesByStatus(List<FormTemplateStatus> statuses, String type, String filter, String sortOrder, String sortBy, int pageNumber, int pageSize)
    {
        List<ResultData> resultDataList = new ArrayList<>();
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.isEmpty() ? "id" : sortBy);
        List<FormTemplate> formTemplates = (type == null || type.trim().equals("") || type.equals("ALL")) ? formTemplateRepository.findByStatusIsIn(statuses, PageRequest.of(pageNumber, pageSize, orderBy)) :
                formTemplateRepository.findByStatusIsInAndType(statuses, FormTemplateType.valueOf(type), PageRequest.of(pageNumber, pageSize, orderBy));
        for (FormTemplate formTemplate : formTemplates)
        {
            ResultData result = new ResultData(true, "");
            result.addProperty("id", formTemplate.getId());
            result.addProperty("name", formTemplate.getName());
            result.addProperty("numberOfForms", formTemplate.getNumberOfForms());
            result.addProperty("totalValue", formTemplate.getTotalValue());
            resultDataList.add(result);
        }
        return resultDataList;
    }

    @Override
    public ResultData getUpdatedFixturesData()
    {
        ResultData resultData = new ResultData(true, "");

        List<FormTemplateStatus> formTemplateStatuses = new ArrayList<>();
        formTemplateStatuses.add(FormTemplateStatus.OPEN);
        formTemplateStatuses.add(FormTemplateStatus.CLOSE);

        resultData.addProperty("matches", getFixtureData(fixtureRepository.findByUsedAndFormTemplateStatusIsInOrderByDateAscTimeAsc(true, formTemplateStatuses)));
        return resultData;
    }

    @Override
    public Long getFormTemplatesCount(List<FormTemplateStatus> statuses, String challengeType)
    {
        if (challengeType == null || challengeType.equals("ALL"))
            return formTemplateRepository.countByStatusIn(statuses);
        return formTemplateRepository.countByStatusInAndType(statuses, FormTemplateType.valueOf(challengeType));
    }

    @Override
    public Long countFormList(Long formTemplateId)
    {
        return formRepository.countByFormTemplate(formTemplateRepository.findById(formTemplateId).get());
    }

    @Override
    public ResultData findFormsByFormTemplate(Long formTemplateId, String filter, String sortOrder, String sortBy, int pageNumber, int pageSize)
    {
        ResultData resultData = new ResultData(true, "");
        if (sortBy.equals("username"))
            sortBy = "account.user.username";
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.isEmpty() ? "id" : sortBy);
        List<Form> forms = formRepository.findByFormTemplate(formTemplateRepository.findById(formTemplateId).get(), PageRequest.of(pageNumber, pageSize, orderBy));
        List<Map<String, String>> dataList = new ArrayList<>();
        for (Form form : forms)
        {
            Map<String, String> data = new HashMap<>();
            data.put("id", form.getId().toString());
            data.put("username", userRepository.findByAccount(form.getAccount()).getUsername());
            data.put("name", form.getName());
            data.put("createdDate", Utills.nameDisplayForDate(form.getCreatedDate(), false));
            data.put("createdTime", Utills.shortDisplayForTime(form.getCreatedTime().toString()));
            data.put("score", String.valueOf(form.getScore()));
            data.put("value", String.valueOf(form.getValue()));
            data.put("status", form.getStatus().name());
            dataList.add(data);
        }
        resultData.addProperty("forms", dataList);
        return resultData;
    }

    @Override
    public ResultData findUserForms(String type, String filter, String sortOrder, String sortBy, int pageNumber, int pageSize)
    {
        if (!userService.isAuthenticated(null))
            return new ResultData(false, "User is not authenticated");
        ResultData resultData = new ResultData(true, type + "Forms returns.");
        List<FormStatus> formStatuses = new ArrayList<>();
        switch (type)
        {
            case "ALL":
                formStatuses.add(FormStatus.REGISTERED);
                formStatuses.add(FormStatus.FINALIZED);
                formStatuses.add(FormStatus.PASSED);
                break;
            case "ACTIVE":
                formStatuses.add(FormStatus.REGISTERED);
                formStatuses.add(FormStatus.FINALIZED);
                break;
            case "INACTIVE":
                formStatuses.add(FormStatus.PASSED);
                break;
        }
        List<Map> forms = new ArrayList<>();
        if (sortBy.equals("templateName"))
            sortBy = "formTemplate.name";
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.isEmpty() ? "id" : sortBy);
        for (Form form : formRepository.findByAccountAndStatusIsIn(userService.getCurrentUser().getAccount(), formStatuses, PageRequest.of(pageNumber, pageSize, orderBy)))
        {
            Map<String, Object> formMap = new HashMap<>();
            formMap.put("id", form.getId());
            formMap.put("name", form.getName());
            formMap.put("value", form.getValue());
            formMap.put("score", form.getScore());
            formMap.put("createdDate", Utills.nameDisplayForDate(form.getCreatedDate(), false));
            formMap.put("createdTime", Utills.shortDisplayForTime(form.getCreatedTime().toString()));
            formMap.put("status", form.getStatus());
            formMap.put("real", form.isReal());
            formMap.put("templateId", form.getFormTemplate().getId());
            formMap.put("templateName", form.getFormTemplate().getName());
            forms.add(formMap);
        }
        resultData.addProperty("forms", forms);
        return resultData;
    }

    @Override
    public Long getUserFormsSize(String type)
    {
        List<FormStatus> formStatuses = new ArrayList<>();
        switch (type)
        {
            case "ALL":
                formStatuses.add(FormStatus.REGISTERED);
                formStatuses.add(FormStatus.FINALIZED);
                formStatuses.add(FormStatus.PASSED);
                break;
            case "ACTIVE":
                formStatuses.add(FormStatus.REGISTERED);
                formStatuses.add(FormStatus.FINALIZED);
                break;
            case "INACTIVE":
                formStatuses.add(FormStatus.PASSED);
                break;
        }
        return formRepository.countByAccountAndStatusIn(userService.getCurrentUser().getAccount(), formStatuses);
    }

    private List<ResultData> getFixtureData(List<Fixture> fixtures)
    {
        List<ResultData> matchResult = new ArrayList<>();
        for (Fixture fixture : fixtures)
        {
            ResultData matchData = new ResultData(true, "");
            matchData.addProperty("id", fixture.getId());
            matchData.addProperty("formTemplateId", fixture.getFormTemplate().getId());
            matchData.addProperty("homeName", Utills.getFarsiName(fixture.getLocalTeamName()));
            matchData.addProperty("homeCountry", Utills.getFarsiName(fixture.getLocalCountryName()));
            matchData.addProperty("homeCountryFlag", fixture.getLocalCountryFlag());
            matchData.addProperty("homeLogo", fixture.getLocalTeamLogo());
            matchData.addProperty("homeScore", fixture.getLocalTeamScore());
            matchData.addProperty("awayName", Utills.getFarsiName(fixture.getVisitorTeamName()));
            matchData.addProperty("awayCountry", Utills.getFarsiName(fixture.getVisitorCountryName()));
            matchData.addProperty("awayCountryFlag", fixture.getVisitorCountryFlag());
            matchData.addProperty("awayLogo", fixture.getVisitorTeamLogo());
            matchData.addProperty("awayScore", fixture.getVisitorTeamScore());
            matchData.addProperty("league", Utills.getFarsiName(fixture.getLeagueName()));
            if( fixture.getLocalTeamCountryId().equals(fixture.getVisitorTeamCountryId()))
                matchData.addProperty("leagueCountry", Utills.getFarsiName(fixture.getVisitorCountryName()));
            matchData.addProperty("time", Utills.shortDisplayForTime(fixture.getTime()));
            matchData.addProperty("date", Utills.nameDisplayForDate(fixture.getDate(), false));
            matchData.addProperty("minute", fixture.getMinute() == null ? "00" : fixture.getMinute().length() == 1 ? '0' + fixture.getMinute() : fixture.getMinute());
//            matchData.addProperty("minute", LocalTime.now().getMinute() < 10 ? '0' + LocalTime.now().getMinute() : LocalTime.now().getMinute());
            matchData.addProperty("status", fixture.getStatus());
            matchResult.add(matchData);
        }
        return matchResult;
    }

    @Override
    public ResultData findUserFormData(Long formId)
    {
        return this.findFormData(formRepository.findById(formId).get());
    }

    @Override
    public ResultData findUserFormData(Long formId, List<FormStatus> statuses)
    {
        Form form = formRepository.findById(formId).get();
        if (statuses.contains(form.getStatus()))
            return this.findFormData(form);
        return new ResultData(false, "Illegal access to form data");
    }

    private ResultData findFormData(Form form)
    {
        ResultData formTemplateData = findFormTemplate(form.getFormTemplate().getId());
        List<Match> matches = matchRepository.findByForm(form);

        for (ResultData data : (List<ResultData>) formTemplateData.getProperties().get("matches"))
            for (Match match : matches)
                if (Long.valueOf(data.getProperties().get("id").toString()).equals(match.getFixtureId()) && match.getForm().getId().equals(form.getId()))
                {
                    data.addProperty("homeWin", match.isLocalWin());
                    data.addProperty("noWin", match.isNoWin());
                    data.addProperty("awayWin", match.isVisitorWin());
                    data.addProperty("score", match.isScore());
                }
        return formTemplateData;
    }

    public ResponseEntity<InputStreamResource> downloadPhotoCal(Long formTemplateId) throws FileNotFoundException
    {
        FormTemplate formTemplate = formTemplateRepository.findById(formTemplateId).get();
        String fileName = "PhotoCal_" + formTemplate.getType().name() + "_" + formTemplate.getId() + ".pdf";
        String desttination = "D://coin/photoCal/" + fileName;

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_PDF);
        respHeaders.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        InputStreamResource isr = new InputStreamResource(new FileInputStream(desttination));
        return new ResponseEntity(isr, respHeaders, HttpStatus.OK);
    }

    private String escapeNonAscii(String str)
    {
        StringBuilder retStr = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            int cp = Character.codePointAt(str, i);
            int charCount = Character.charCount(cp);
            if (charCount > 1)
            {
                i += charCount - 1; // 2.
                if (i >= str.length())
                {
                    throw new IllegalArgumentException("truncated unexpectedly");
                }
            }

            if (cp < 128)
            {
                retStr.appendCodePoint(cp);
            } else
            {
                retStr.append("\\").append(String.format("u0%x", cp));
            }
        }
        return retStr.toString();
    }
}
