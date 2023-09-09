package me.hashemalayan.services.db;

import btree4j.BTreeCallback;
import btree4j.BTreeException;
import btree4j.BTreeIndex;
import btree4j.Value;
import btree4j.indexer.BasicIndexQuery;
import btree4j.indexer.BasicIndexQuery.IndexConditionEQ;
import me.hashemalayan.NodeProperties;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IndexService {

    private final Path collectionsPath;

    public IndexService(NodeProperties nodeProperties) {
        collectionsPath = Paths.get(nodeProperties.getName(), "collections");
    }

    public IndexService(String nodeName) {
        collectionsPath = Paths.get("/home/mythi/development/all_work/nosql/nosql/", nodeName, "collections");
    }

    void indexPropertyInCollection(String collectionId, String property) throws IOException, BTreeException {

        final var indexesPath = collectionsPath.resolve(collectionId).resolve("indexes");
        if (!Files.exists(indexesPath))
            Files.createDirectories(indexesPath);

        final var indexFilePath = indexesPath.resolve(collectionId + "_" + property + ".idx");

        final var bTreeIndex = new BTreeIndex(
                indexFilePath.toFile(),
                true
        );
        bTreeIndex.init(true);

        bTreeIndex.addValue(new Value("hi"), new Value("hello1"));
        bTreeIndex.addValue(new Value("hi"), new Value("hello2"));
        bTreeIndex.search(
                new BasicIndexQuery.IndexConditionANY(),
                new BTreeCallback() {
                    @Override
                    public boolean indexInfo(Value value, long pointer) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean indexInfo(Value key, byte[] value) {
                        System.out.println(new String(value, StandardCharsets.UTF_16));
                        return true;
                    }
                }
        );
        bTreeIndex.flush();
        bTreeIndex.close();
    }
}
