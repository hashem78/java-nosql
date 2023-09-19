package me.hashemalayan.services.db;

import btree4j.BTreeException;
import btree4j.BTreeIndexDup;
import btree4j.Value;
import btree4j.indexer.LikeIndexQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import me.hashemalayan.nosql.shared.Customstruct;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.IndexNotFoundException;
import me.hashemalayan.services.db.exceptions.InvalidOperatorUsage;
import me.hashemalayan.services.db.exceptions.UnRecognizedOperatorException;
import me.hashemalayan.services.db.interfaces.CollectionConfigurationService;
import me.hashemalayan.util.BTreeCallbackFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static btree4j.indexer.BasicIndexQuery.*;

record CollectionPropertyPair(String collectionId, String propertyName) {

}

public class IndexService {

    private final JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory;

    private final BTreeCallbackFactory bTreeCallbackFactory;

    private final CollectionConfigurationService configurationService;

    private final ObjectMapper objectMapper;

    private final Logger logger;

    private final Path collectionsPath;
    private final Map<CollectionPropertyPair, BTreeIndexDup> indexMap;

    @Inject
    public IndexService(
            JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory,
            BTreeCallbackFactory bTreeCallbackFactory,
            CollectionConfigurationService configurationService,
            ObjectMapper objectMapper,
            Logger logger,
            NodeProperties nodeProperties
    ) {
        this.jsonDirectoryIteratorFactory = jsonDirectoryIteratorFactory;
        this.bTreeCallbackFactory = bTreeCallbackFactory;
        this.configurationService = configurationService;
        this.objectMapper = objectMapper;
        this.logger = logger;
        collectionsPath = Paths.get(nodeProperties.getName(), "collections");
        indexMap = new ConcurrentHashMap<>();
    }

    public void load() throws IOException, BTreeException {

        try (final var collectionPathStream = Files.newDirectoryStream(collectionsPath)) {
            for (final var collectionPath : collectionPathStream) {
                if (!Files.isDirectory(collectionPath)) continue;
                final var collectionId = collectionPath.getFileName().toString();
                final var collectionIndexesPath = collectionPath.resolve("indexes");
                if (!Files.exists(collectionIndexesPath))
                    Files.createDirectories(collectionIndexesPath);
                try (final var collectionIndexesPathStream = Files.newDirectoryStream(collectionIndexesPath)) {
                    for (final var collectionIndexPath : collectionIndexesPathStream) {
                        final var propertyName = collectionIndexPath.getFileName().toString();
                        final var bTree = new BTreeIndexDup(collectionIndexPath.toFile());
                        bTree.init(false);
                        indexMap.put(new CollectionPropertyPair(collectionId, propertyName), bTree);
                    }
                }
            }
        }
    }

    public void indexPropertyInCollection(String collectionId, String property) throws
            IOException,
            BTreeException,
            CollectionDoesNotExistException {

        final var collectionPath = collectionsPath.resolve(collectionId);
        final var collectionIndexesPath = collectionsPath.resolve(collectionId).resolve("indexes");
        final var indexFilePath = collectionIndexesPath.resolve(property);

        if (!Files.exists(collectionPath))
            throw new CollectionDoesNotExistException();

        if (!Files.exists(collectionIndexesPath))
            Files.createDirectories(collectionIndexesPath);

        final var bTreeIndex = new BTreeIndexDup(indexFilePath.toFile());
        bTreeIndex.init(false);

        final var documentsPath = collectionPath.resolve("documents");

        final var documentsIterator = jsonDirectoryIteratorFactory.create(documentsPath);
        while (documentsIterator.hasNext()) {
            final var iteratorResult = documentsIterator.next();
            final var documentPath = collectionPath.resolve(iteratorResult.documentName());
            logger.debug("Indexing " + documentPath);
            final var documentNode = iteratorResult.jsonNode();
            if (!documentNode.has("data")) continue;
            final var dataNode = documentNode.get("data");
            if (!dataNode.has(property)) continue;
            final var valueOfProperty = dataNode.get(property);

            bTreeIndex.addValue(
                    new Value(objectMapper.writeValueAsBytes(valueOfProperty)),
                    new Value("\"" + FilenameUtils.removeExtension(iteratorResult.documentName()) + "\"")
            );
        }
        bTreeIndex.flush();
        indexMap.put(
                new CollectionPropertyPair(collectionId, property),
                bTreeIndex
        );
        configurationService.editCollectionMetaData(
                collectionId,
                (metaData) -> metaData.addIndexedProperties(property)
        );
    }

    private void addToIndex(CollectionPropertyPair pair, String documentId, byte[] value)
            throws BTreeException {

        final var index = indexMap.get(pair);

        index.addValue(
                new Value(value),
                new Value("\"" + documentId + "\"")
        );
        index.flush();
    }


    public void addToIndex(
            String collectionId,
            String documentId,
            String property,
            byte[] newKeyBytes)
            throws IndexNotFoundException,
            BTreeException {

        final var pair = new CollectionPropertyPair(collectionId, property);
        if (!indexMap.containsKey(pair)) {
            throw new IndexNotFoundException();
        }

        addToIndex(pair, documentId, newKeyBytes);
    }


    /**
     * @throws IllegalArgumentException if previousKeyBytes is null.
     */
    public void addToIndex(
            String collectionId,
            String documentId,
            String property,
            byte[] previousKeyBytes,
            byte[] newKeyBytes)
            throws IndexNotFoundException,
            IllegalArgumentException,
            BTreeException {

        final var pair = new CollectionPropertyPair(collectionId, property);
        if (!indexMap.containsKey(pair)) {
            throw new IndexNotFoundException();
        }
        final var index = indexMap.get(pair);

        if (previousKeyBytes == null) {
            throw new IllegalArgumentException("previousKeyBytes should not be null");
        }

        final var bTreeValueForPreviousKey = new Value(previousKeyBytes);
        final var previousValueInIndex = index.getValue(bTreeValueForPreviousKey);

        // This means that the key is unique in the index.

        if (previousValueInIndex == null) {
            addToIndex(pair, documentId, newKeyBytes);
            return;
        }
        index.removeValue(bTreeValueForPreviousKey);
        addToIndex(pair, documentId, newKeyBytes);
    }

    public boolean isPropertyIndexed(String collectionId, String property) {

        return getIndexedProperties(collectionId).contains(property);
    }

    public List<String> getIndexedProperties(String collectionId) {
        final var metaDataOpt = configurationService.getCollectionMetaData(collectionId);

        if (metaDataOpt.isEmpty())
            return new ArrayList<>();

        final var metaData = metaDataOpt.get();
        return metaData.getIndexedPropertiesList();
    }

    public void removeIndexFromCollectionProperty(String collectionId, String property)
            throws IndexNotFoundException,
            BTreeException,
            IOException, CollectionDoesNotExistException {

        final var pair = new CollectionPropertyPair(collectionId, property);
        if (!indexMap.containsKey(pair))
            throw new IndexNotFoundException();

        final var collectionIndexesPath = collectionsPath.resolve(collectionId).resolve("indexes");
        final var indexFilePath = collectionIndexesPath.resolve(property);

        final var index = indexMap.get(pair);
        index.close();
        indexMap.remove(pair);
        configurationService.editCollectionMetaData(
                collectionId, x -> {
                    final var props = new ArrayList<>(x.getIndexedPropertiesList());
                    props.remove(property);
                    return x.clearIndexedProperties().addAllIndexedProperties(props);
                }
        );
        Files.deleteIfExists(indexFilePath);
    }

    public void runQuery(
            String collectionId,
            Operator operator,
            String property,
            Customstruct.CustomValue value,
            Consumer<String> responseConsumer
    ) throws IndexNotFoundException,
            BTreeException,
            InvalidOperatorUsage,
            UnRecognizedOperatorException {
        final var pair = new CollectionPropertyPair(collectionId, property);

        if (!indexMap.containsKey(pair))
            throw new IndexNotFoundException();

        final var index = indexMap.get(pair);

        final var valueBytes = getCustomValueBytes(value);
        final var bTreeValue = new Value(valueBytes);
        final var adapter = bTreeCallbackFactory.create(
                (k, v) -> {
                    responseConsumer.accept(v);
                    return true;
                }
        );

        switch (operator) {

            case EQUALS -> index.search(new IndexConditionEQ(bTreeValue), adapter);
            case NOT_EQUALS -> index.search(new IndexConditionNE(bTreeValue), adapter);
            case GREATER_THAN -> index.search(new IndexConditionGT(bTreeValue), adapter);
            case LESS_THAN -> index.search(new IndexConditionLT(bTreeValue), adapter);
            case GREATER_THAN_OR_EQUALS -> index.search(new IndexConditionGE(bTreeValue), adapter);
            case LESS_THAN_OR_EQUALS -> index.search(new IndexConditionLE(bTreeValue), adapter);
            case STARTS_WITH -> {
                final var noEndQuotes = Arrays.copyOfRange(valueBytes, 0, valueBytes.length - 1);
                index.search(new LikeIndexQuery(new Value(noEndQuotes), "%"), adapter);
            }
            case IN -> index.search(new IndexConditionIN(decodeAndMapValue(value)), adapter);
            case NOT_IN -> index.search(new IndexConditionNIN(decodeAndMapValue(value)), adapter);
            case UNRECOGNIZED -> throw new UnRecognizedOperatorException();
        }
    }

    private Object getCustomValueBytesHelper(Customstruct.CustomValue value) {
        return switch (value.getValueCase()) {

            case STRING_VALUE -> value.getStringValue();
            case INT_VALUE -> value.getIntValue();
            case LIST_VALUE -> value.getListValue().getValuesList()
                    .stream()
                    .map(this::getCustomValueBytesHelper)
                    .toList();
            default -> "";
        };
    }

    private byte[] getCustomValueBytes(Customstruct.CustomValue value) {

        try {
            return objectMapper.writeValueAsBytes(getCustomValueBytesHelper(value));
        } catch (JsonProcessingException e) {
            return new byte[]{};
        }

    }

    private Value[] decodeAndMapValue(Customstruct.CustomValue value) throws InvalidOperatorUsage {

        if (!value.hasListValue())
            throw new InvalidOperatorUsage();

        return value.getListValue()
                .getValuesList()
                .stream()
                .map(this::getCustomValueBytes)
                .map(Value::new)
                .toArray(Value[]::new);
    }
}