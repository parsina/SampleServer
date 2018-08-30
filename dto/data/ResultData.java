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

    public Object getPropertyValue(String key)
    {
        return properties.get(key);
    }

//    public JsonObject getJsonObject()
//    {
//        JsonObject json = new JsonObject();
//        json.addProperty("success", isSuccess());
//        json.addProperty("msg", getMessage());
//        for (Map.Entry<String, Object> entry : properties.entrySet())
//        {
//            Object object = entry.getValue();
//            if (object == null)
//                json.add(entry.getKey(), JsonNull.INSTANCE);
//            else if (object instanceof JsonElement)
//                json.add(entry.getKey(), (JsonElement) object);
//            else if (object instanceof Number)
//                json.addProperty(entry.getKey(), (Number) object);
//            else if (object instanceof Boolean)
//                json.addProperty(entry.getKey(), (Boolean) object);
//            else if (object instanceof Character)
//                json.addProperty(entry.getKey(), (Character) object);
//            else
//                json.addProperty(entry.getKey(), object.toString());
//        }
//
//        return json;
//    }
}
