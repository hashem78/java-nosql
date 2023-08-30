package me.hashemalayan.db;

import btree4j.*;
import btree4j.indexer.BasicIndexQuery;
import btree4j.utils.lang.Primitives;
import com.google.inject.Inject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class DBManager {

    private final BTreeIndexDup bTree;

    private final Logger logger;

    @Inject
    public DBManager(BTreeIndexDup bTree, Logger logger) {
        this.bTree = bTree;
        this.logger = logger;
    }

    public void addStuff() throws BTreeException {
        bTree.addValue(new Value("temp"), new Value("Hi this is hashem3!"));
    }

    public void getAllStuff() throws BTreeException, IOException {
        var value = bTree.getValue(new Value("temp"));
        assert value != null;
        System.out.println("CONTENT LENGTH IS: " + value.getLength());
        var inputStream = Objects.requireNonNull(value).getInputStream();
        var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_16));
        var stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Here");
            stringBuilder.append(line).append("\n");
        }
        logger.debug("DBManager.getAllStuff: " + stringBuilder.toString().trim());
    }
}
