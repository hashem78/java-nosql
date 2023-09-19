package me.hashemalayan.services.db.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.nosql.shared.CollectionPropertyType;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.PropertyDoesNotExistException;
import me.hashemalayan.services.db.exceptions.SampleMalformedException;

import java.util.Set;

public interface SchemaService {
    void load();

    String getSample(String collectionId)
            throws CollectionDoesNotExistException,
            SampleMalformedException,
            JsonProcessingException;

    Set<ValidationMessage> validateDocument(String collectionName, JsonNode jsonNode);

    Set<ValidationMessage> validateDocument(
            String collectionName,
            String jsonDocument
    ) throws JsonProcessingException;

    void validateAll();

    CollectionPropertyType getPropertyType(String collectionId, String property)
            throws CollectionDoesNotExistException,
            PropertyDoesNotExistException;
}
