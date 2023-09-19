package me.hashemalayan.services.db.interfaces;

import btree4j.BTreeException;
import me.hashemalayan.nosql.shared.Customstruct;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.IndexNotFoundException;
import me.hashemalayan.services.db.exceptions.InvalidOperatorUsage;
import me.hashemalayan.services.db.exceptions.UnRecognizedOperatorException;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface IndexService {
    void load() throws IOException, BTreeException;

    void indexPropertyInCollection(String collectionId, String property)
            throws IOException,
            BTreeException,
            CollectionDoesNotExistException;

    void addToIndex(
            String collectionId,
            String documentId,
            String property,
            byte[] newKeyBytes)
            throws IndexNotFoundException,
            BTreeException;

    /**
     * @throws IllegalArgumentException if previousKeyBytes is null.
     */
    void addToIndex(
            String collectionId,
            String documentId,
            String property,
            byte[] previousKeyBytes,
            byte[] newKeyBytes)
            throws IndexNotFoundException,
            IllegalArgumentException,
            BTreeException;

    List<String> getIndexedProperties(String collectionId);

    boolean isPropertyIndexed(String collectionId, String property);

    void removeIndexFromCollectionProperty(String collectionId, String property)
            throws IndexNotFoundException,
            BTreeException,
            IOException, CollectionDoesNotExistException;

    void runQuery(
            String collectionId,
            Operator operator,
            String property,
            Customstruct.CustomValue value,
            Consumer<String> responseConsumer
    ) throws IndexNotFoundException,
            BTreeException,
            InvalidOperatorUsage,
            UnRecognizedOperatorException;
}
