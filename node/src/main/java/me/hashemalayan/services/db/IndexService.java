package me.hashemalayan.services.db;

import btree4j.*;
import btree4j.indexer.BasicIndexQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.IndexNotFoundException;
import me.hashemalayan.util.BTreeCallbackFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

record CollectionPropertyPair(String collectionId, String propertyName) {

}

public class IndexService {

    private final JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory;

    private final BTreeCallbackFactory bTreeCallbackFactory;

    private final ObjectMapper objectMapper;

    private final Logger logger;

    private final Path collectionsPath;
    private final Map<CollectionPropertyPair, BTreeIndexDup> indexMap;

    @Inject
    public IndexService(
            JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory,
            BTreeCallbackFactory bTreeCallbackFactory,
            ObjectMapper objectMapper,
            Logger logger,
            NodeProperties nodeProperties
    ) {
        this.jsonDirectoryIteratorFactory = jsonDirectoryIteratorFactory;
        this.bTreeCallbackFactory = bTreeCallbackFactory;
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
            logger.info(Arrays.toString(objectMapper.writeValueAsBytes(valueOfProperty)));
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
    }

    public void getEqual(
            String collectionId,
            String property,
            String value,
            Consumer<String> documentIdsConsumer
    ) throws IndexNotFoundException, BTreeException, JsonProcessingException {

        final var pair = new CollectionPropertyPair(collectionId, property);
        if (!indexMap.containsKey(pair))
            throw new IndexNotFoundException();

        final var index = indexMap.get(pair);
        final var valueAsBytes = objectMapper.writeValueAsBytes(value);
        final var query = new BasicIndexQuery.IndexConditionEQ(new Value(valueAsBytes));

        index.search(query, bTreeCallbackFactory.create(
                (k, v) -> {
                    documentIdsConsumer.accept(v.asText());
                    return true;
                }
        ));
    }

}

