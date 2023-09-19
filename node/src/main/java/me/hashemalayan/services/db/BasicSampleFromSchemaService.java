package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import me.hashemalayan.services.db.interfaces.SampleFromSchemaService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicSampleFromSchemaService implements SampleFromSchemaService {
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> collectionIdSampleMap;

    @Inject
    public BasicSampleFromSchemaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        collectionIdSampleMap = new ConcurrentHashMap<>();
    }

    @Override
    public JsonNode getSample(String collectionId, JsonNode schema) {

        if (!collectionIdSampleMap.containsKey(collectionId))
            collectionIdSampleMap.put(collectionId, getSampleHelper(schema));

        return collectionIdSampleMap.get(collectionId);
    }

    private JsonNode getSampleHelper(JsonNode schema) {

        var resultNode = objectMapper.createObjectNode();
        if (schema.has("properties")) {

            JsonNode propertiesNode = schema.get("properties");
            propertiesNode.fields().forEachRemaining(
                    entry -> {
                        String key = entry.getKey();
                        JsonNode propertySchema = entry.getValue();

                        if (propertySchema.has("type")) {
                            String type = propertySchema.get("type").asText();

                            switch (type) {
                                case "object" -> resultNode.set(key, getSampleHelper(propertySchema));
                                case "string" -> resultNode.put(key, "");
                                case "boolean" -> resultNode.put(key, false);
                                case "array" -> resultNode.set(key, objectMapper.createArrayNode());
                                case "integer", "number" -> resultNode.put(key, 0);
                            }
                        }
                    }
            );
        }
        return resultNode;
    }
}
