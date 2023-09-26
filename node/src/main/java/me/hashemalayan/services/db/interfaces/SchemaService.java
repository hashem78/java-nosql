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

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws SampleMalformedException if the sample is malformed.
     */
    String getSample(String collectionId);

    Set<ValidationMessage> validateDocument(String collectionName, JsonNode jsonNode);

    Set<ValidationMessage> validateDocument(
            String collectionName,
            String jsonDocument
    );

    void validateAll();

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws PropertyDoesNotExistException if the property does not exist.
     */
    CollectionPropertyType getPropertyType(String collectionId, String property);
}
