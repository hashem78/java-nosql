package me.hashemalayan.services.db;

import btree4j.BTreeException;
import me.hashemalayan.NodeProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class IndexServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void indexPropertyInCollection() throws BTreeException, IOException {

        final var service = new IndexService("node2");
        service.indexPropertyInCollection(
                "139b6cc9-8582-4270-847f-273ac605cb04",
                "name"
        );
    }
}