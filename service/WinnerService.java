package com.coin.app.service;

import com.coin.app.dto.data.ResultData;

public interface WinnerService
{
    Long countWinnerList(Long formTemplateId);

    ResultData findWinnersByFormTemplate(Long formTemplateId, String filter, String sortOrder, String sortBy, int pageNumber,int pageSize);
}
