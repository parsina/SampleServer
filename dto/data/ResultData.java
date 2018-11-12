package com.coin.app.dto.data;

import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode

public class ResultData
{
    private boolean success;
    private String message;
    private Map<String, Object> properties;

    public ResultData(boolean success, String message)
    {
        this.success = success;
        this.message = message;
        properties = new HashMap<>();
    }

    public void addProperty(String key, Object value)
    {
        properties.put(key, value);
    }
}
