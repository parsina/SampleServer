package com.coin.app.service;

import java.util.List;
import java.util.Set;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Form;
import reactor.core.publisher.Flux;

public interface FormService
{
    ResultData findFormTemplate(Long formId);

    List<ResultData>  createFormTemplate(List<Long> matchIds);

    ResultData  createForm(Long formTemplateId, Long userId, List<ResultData> matchesData);

    List<ResultData> findFormTemplates();

    ResultData getUpdatedFixturesData();

    List<ResultData>  createOrUpdateForm(List<ResultData> formData);
}
