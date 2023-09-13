package me.hashemalayan.services.db;

import btree4j.BTreeException;
import btree4j.BTreeIndexDup;
import btree4j.Value;
import btree4j.indexer.LikeIndexQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import me.hashemalayan.nosql.shared.Customstruct;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.IndexNotFoundException;
import me.hashemalayan.services.db.exceptions.InvalidOperatorUsage;
import me.hashemalayan.services.db.exceptions.UnRecognizedOperatorException;
import me.hashemalayan.util.BTreeCallbackFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean isPropertyIndexed(String collectionId, String property) {

        final var metaDataOpt = configurationService.getCollectionMetaData(collectionId);

        if (metaDataOpt.isEmpty())
            return false;

        final var metaData = metaDataOpt.get();
        return metaData.getIndexedPropertiesList().contains(property);
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
            JsonProcessingException,
            BTreeException,
            InvalidOperatorUsage,
            UnRecognizedOperatorException {
        final var pair = new CollectionPropertyPair(collectionId, property);

        if (!indexMap.containsKey(pair))
            throw new IndexNotFoundException();

        final var index = indexMap.get(pair);

        final var valueBytes = switch (value.getValueCase()) {

            case STRING_VALUE -> objectMapper.writeValueAsBytes(value.getStringValue());
            case INT_VALUE -> objectMapper.writeValueAsBytes(value.getIntValue());
            default -> objectMapper.writeValueAsBytes("");
        };
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
            case IN -> index.search(new IndexConditionIN(decodeAndMapValue(value.getStringValue())), adapter);
            case NOT_IN -> index.search(new IndexConditionNIN(decodeAndMapValue(value.getStringValue())), adapter);
            case UNRECOGNIZED -> throw new UnRecognizedOperatorException();
        }
    }

    private Value[] decodeAndMapValue(String value) throws InvalidOperatorUsage, JsonProcessingException {
        final var jsonNode = objectMapper.readTree(value);
        if (!jsonNode.isArray())
            throw new InvalidOperatorUsage();
        final var valueList = new ArrayList<Value>();
        final var arrayNode = (ArrayNode) jsonNode;
        for (final var node : arrayNode) {
            valueList.add(new Value(objectMapper.writeValueAsBytes(node)));
        }
        return valueList.toArray(Value[]::new);
    }
}