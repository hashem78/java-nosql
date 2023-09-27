package me.hashemalayan.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import me.hashemalayan.nosql.shared.Customstruct.CustomList;
import me.hashemalayan.nosql.shared.Customstruct.CustomStruct;
import me.hashemalayan.nosql.shared.Customstruct.CustomValue;

public class CustomStructToJson {
    private final ObjectMapper objectMapper;

    @Inject
    public CustomStructToJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode convertCustomStructToJson(CustomStruct customStruct) {
        ObjectNode jsonObject = objectMapper.createObjectNode();

        for (String key : customStruct.getFieldsMap().keySet()) {
            CustomValue value = customStruct.getFieldsOrThrow(key);
            jsonObject.set(key, convertCustomValueToJson(value));
        }

        return jsonObject;
    }

    private JsonNode convertCustomValueToJson(CustomValue customValue) {
        return switch (customValue.getValueCase()) {
            case STRING_VALUE -> objectMapper.valueToTree(customValue.getStringValue());
            case INT_VALUE -> objectMapper.valueToTree(customValue.getIntValue());
            case DOUBLE_VALUE -> objectMapper.valueToTree(customValue.getDoubleValue());
            case BOOL_VALUE -> objectMapper.valueToTree(customValue.getBoolValue());
            case STRUCT_VALUE -> convertCustomStructToJson(customValue.getStructValue());
            case LIST_VALUE -> convertCustomListToJson(customValue.getListValue());
            case NULL_VALUE -> objectMapper.nullNode();
            default ->
                    throw new IllegalArgumentException("Unsupported CustomValue type: " + customValue.getValueCase());
        };
    }

    private JsonNode convertCustomListToJson(CustomList customList) {
        var jsonArray = objectMapper.createArrayNode();

        for (CustomValue value : customList.getValuesList()) {
            JsonNode jsonValue = convertCustomValueToJson(value);
            jsonArray.add(jsonValue);
        }
        return jsonArray;
    }
}
