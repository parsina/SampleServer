package com.coin.app.service;

import java.util.Set;

import com.coin.app.model.livescore.Form;
import com.coin.app.repository.FormRepository;
import com.coin.app.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class FormServiceImpl implements FormService
{
    @Autowired
    private FormRepository formRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Override
    public Form createForm(Set<Long> matchIds)
    {
        Form form = new Form("فرم شماره " + formRepository.count());
        form.setMatches(matchRepository.findAllById(matchIds));
        return formRepository.save(form);
    }
}
