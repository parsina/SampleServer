package com.coin.app.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.enums.FormStatus;
import com.coin.app.model.enums.FormTemplateStatus;
import com.coin.app.model.enums.FormTemplateType;
import com.coin.app.repository.UserRepository;
import com.coin.app.service.FormService;
import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
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
//@CrossOrigin(origins = {"http://localhost:8080"}, maxAge = 4800, allowCredentials = "false")
public class FormController
{
    @Autowired
    private LiveScoreService liveScoreService;

    @Autowired
    private FormService formService;

    @Secured("ROLE_ADMIN")
    @PostMapping("/loadFixturesData")
    public void loadFixturesData()
    {
        liveScoreService.loadFixtures();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/fixtureData")
    public List<ResultData> freeFixtureData()
    {
        return liveScoreService.findAllFreeFixtures();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/createFormTemplate")
    public List<ResultData> createFormTemplate(@RequestBody Map<String, ?> input)
    {
        return formService.createFormTemplate((List) input.get("ids"), FormTemplateType.valueOf(input.get("type").toString()));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/deleteFormTemplate")
    public ResultData deleteFormTemplate(@RequestBody Map<String, ?> input)
    {
        return formService.deleteFormTemplate(Long.valueOf(input.get("formTemplateId").toString()));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/deleteUserForm")
    public ResultData deleteUserForm(@RequestBody Map<String, ?> input)
    {
        return formService.deleteUserForm(Long.valueOf(input.get("formId").toString()));
    }

    @PostMapping("/formTemplates")
    public List<ResultData> formTemplates()
    {
        List<FormTemplateStatus> statuses = new ArrayList<>();
        statuses.add(FormTemplateStatus.OPEN);
        statuses.add(FormTemplateStatus.CLOSE);
        return formService.findFormTemplatesByStatus(statuses);
    }

    @PostMapping("/formTemplateData")
    public ResultData formTemplateData(@RequestBody Map<String, Long> input)
    {
        return formService.findFormTemplate(input.get("id"));
    }

    @GetMapping("/updateFormTemplate")
    public SseEmitter updateFTData()
    {
        SseEmitter notifier = new SseEmitter(60000L);
        try
        {
            ResultData data = formService.getUpdatedFixturesData();
            notifier.send(SseEmitter.event().reconnectTime(500).data(data));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println(">>>>> Update Time : " + new Date());
        return notifier;
    }


    @PostMapping("/totalPassedFromTemplates")
    public Long totalPassedFromTemplates(@RequestBody Map<String, String> input)
    {
        List<FormTemplateStatus> statuses = new ArrayList<>();
        statuses.add(FormTemplateStatus.PASSED);
        return formService.getFormTemplatesCount(statuses, input.get("challengeType"));
    }

    @GetMapping("/downloadPhotoCal")
    public ResponseEntity<InputStreamResource> downloadPhotoCal(Long formTemplateId) throws FileNotFoundException
    {
        return formService.downloadPhotoCal(formTemplateId);
    }


    @PostMapping("/passedFormTemplates")
    public List<ResultData> passedFromTemplates(@RequestBody Map<String, String> input)
    {
        List<FormTemplateStatus> statuses = new ArrayList<>();
        statuses.add(FormTemplateStatus.PASSED);
        return formService.findFormTemplatesByStatus(statuses, input.get("challengeType"), input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }

    @PostMapping("/formListSize")
    public Long templateFormsSize(@RequestBody Map<String, Long> input)
    {
        return formService.countFormList(input.get("formTemplateId"));
    }

    @PostMapping("/formList")
    public ResultData templateForms(@RequestBody Map<String, String> input)
    {
        return formService.findFormsByFormTemplate(Long.valueOf(input.get("formTemplateId")), input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/openFormTemplates")
    public List<ResultData> openFormTemplates()
    {
        List<FormTemplateStatus> statuses = new ArrayList<>();
        statuses.add(FormTemplateStatus.OPEN);
        return formService.findFormTemplatesByStatus(statuses);
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/createForm")
    public ResultData createForm(@RequestBody Map<String, Object> input)
    {
        return formService.createForm(Long.valueOf(input.get("formTemplateId").toString()), Long.valueOf(input.get("userId").toString()), Boolean.valueOf(input.get("real").toString()), (List<ResultData>) input.get("formData"));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/updateForm")
    public ResultData updateForm(@RequestBody Map<String, Object> input)
    {
        return formService.updateForm(Long.valueOf(input.get("formId").toString()), Long.valueOf(input.get("userId").toString()), Boolean.valueOf(input.get("real").toString()), (List<ResultData>) input.get("formData"));
    }

    @PostMapping("/finalizeFormsData")
    public ResultData finalizeFormsData(@RequestBody Map<String, String> input)
    {
        return formService.findFinalizedForms(input.get("templateId"), input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }

    @PostMapping("/finalizeFormsCount")
    public Long finalizeFormsCount(@RequestBody Map<String, String> input)
    {
        return formService.countFinalizedForms(input.get("templateId"));
    }


    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/userForms")
    public ResultData userForms(@RequestBody Map<String, String> input)
    {
        return formService.findUserForms(input.get("formType"), input.get("filter"), input.get("sortOrder"), input.get("sortBy"), Integer.valueOf(input.get("pageNumber")), Integer.valueOf(input.get("pageSize")));
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PostMapping("/userFormsCount")
    public Long userFormsCount(@RequestBody Map<String, String> input)
    {
        return formService.getUserFormsSize(input.get("formType"));
    }

    @PostMapping("/getUserFormData")
    public ResultData findUserFormData(@RequestBody Map<String, String> input)
    {
        return formService.findUserFormData(Long.valueOf(input.get("formId")));
    }

    @PostMapping("/getPassedFormData")
    public ResultData findPassedFormData(@RequestBody Map<String, String> input)
    {
        List<FormStatus> statuses = new ArrayList<>();
        statuses.add(FormStatus.PASSED);
        return formService.findUserFormData(Long.valueOf(input.get("formId")), statuses);
    }
}
