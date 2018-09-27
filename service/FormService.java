package com.coin.app.service;

import java.util.List;
import java.util.Set;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.enums.FormStatus;
import com.coin.app.model.enums.FormTemplateStatus;
import com.coin.app.model.enums.FormTemplateType;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormTemplate;
import reactor.core.publisher.Flux;

public interface FormService
{
    ResultData findFormTemplate(Long formId);

    List<ResultData>  createFormTemplate(List matchIds, FormTemplateType type);

    ResultData  createForm(Long formTemplateId, Long userId, List<ResultData> matchesData);

    ResultData  updateForm(Long formId, Long userId, List<ResultData> matchesData);

    List<ResultData> findFormTemplatesByStatus(List<FormTemplateStatus> statuses);

    List<ResultData> findFormTemplatesByStatus(List<FormTemplateStatus> statuses, String filter, String sortOrder, String sortBy, int pageNumber,int pageSize);

    ResultData getUpdatedFixturesData();

    ResultData findUserForms(String type, String filter, String sortOrder, String sortBy, int pageNumber,int pageSize);

    Long getFormTemplatesCount(List<FormTemplateStatus> statuses);

    Long getTemplateFormssCount(Long formTemplateId);

    ResultData findFormsByFormTemplate(Long formTemplateId, String filter, String sortOrder, String sortBy, int pageNumber,int pageSize);

    Long getUserFormsSize(String type);

    ResultData findUserFormData(Long formId);

    ResultData findUserFormData(Long formId, List<FormStatus> statuses);
}
