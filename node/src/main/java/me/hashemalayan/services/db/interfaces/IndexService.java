package me.hashemalayan.services.db.interfaces;

import me.hashemalayan.nosql.shared.Customstruct;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.services.db.exceptions.*;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;

public interface IndexService {
    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     */
    void load();

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     */
    void indexPropertyInCollection(String collectionId, String property);

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     */
    void addToIndex(
            String collectionId,
            String documentId,
            String property,
            byte[] newKeyBytes);

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws IllegalArgumentException if previousKeyBytes is null.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     */
    void addToIndex(
            String collectionId,
            String documentId,
            String property,
            byte[] previousKeyBytes,
            byte[] newKeyBytes);

    List<String> getIndexedProperties(String collectionId);

    boolean isPropertyIndexed(String collectionId, String property);

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     */
    void removeIndexFromCollectionProperty(String collectionId, String property);

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws InvalidOperatorUsage if the operator usage is invalid.
     * @throws UnRecognizedOperatorException if the operator is unrecognized.
     */
    void runQuery(
            String collectionId,
            Operator operator,
            String property,
            Customstruct.CustomValue value,
            Consumer<String> responseConsumer);

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws InvalidOperatorUsage if the operator usage is invalid.
     * @throws UnRecognizedOperatorException if the operator is unrecognized.
     */
    List<String> runQuery(
            String collectionId,
            Operator operator,
            String property,
            Customstruct.CustomValue value);
}
