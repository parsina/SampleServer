package com.coin.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Winner;
import com.coin.app.repository.FormTemplateRepository;
import com.coin.app.repository.WinnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WinnerServiceImpl implements WinnerService
{
    @Autowired
    private WinnerRepository winnerRepository;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Override
    public Long countWinnerList(Long formTemplateId)
    {
        return winnerRepository.countByFormFormTemplate(formTemplateRepository.findById(formTemplateId).get());
    }

    @Override
    public ResultData findWinnersByFormTemplate(Long formTemplateId, String filter, String sortOrder, String sortBy, int pageNumber, int pageSize)
    {
        ResultData resultData = new ResultData(true, "");
//        if (sortBy.equals("username"))
//            sortBy = "account.user.username";
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.isEmpty() ? "id" : sortBy);
        List<Winner> winners = winnerRepository.findByFormFormTemplate(formTemplateRepository.findById(formTemplateId).get(), PageRequest.of(pageNumber, pageSize, orderBy));
        List<Map<String, String>> dataList = new ArrayList<>();
        for (Winner winner : winners)
        {
            Map<String, String> data = new HashMap<>();
            data.put("id", winner.getId().toString());
            data.put("username", winner.getForm().getAccount().getUser().getUsername());
            data.put("prize", winner.getPrize().toString());
            data.put("winnerPlace", winner.getWinnerPlace().name());
            data.put("formId", winner.getForm().getId().toString());
            data.put("formName", winner.getForm().getName());
            data.put("formValue", Long.valueOf(winner.getForm().getValue()).toString());
            data.put("formScore", Integer.valueOf(winner.getForm().getScore()).toString());
            dataList.add(data);
        }
        resultData.addProperty("winners", dataList);
        return resultData;
    }
}
