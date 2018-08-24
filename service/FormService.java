package com.coin.app.service;

import java.util.List;
import java.util.Set;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormType;

public interface FormService
{
    ResultData findForm(Long formId);

    Form createForm(List<Long> matchIds, FormType type);

    List<ResultData> findAllFreeMatches(int priority);

    List<ResultData> findForms(FormType type);
}
