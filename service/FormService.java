package com.coin.app.service;

import java.util.Set;

import com.coin.app.model.livescore.Form;

public interface FormService
{
    Form createForm(Set<Long> matchIds);
}
