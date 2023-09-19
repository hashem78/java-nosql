package me.hashemalayan.services.db.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

public interface SampleFromSchemaService {
    JsonNode getSample(String collectionId, JsonNode schema);
}
