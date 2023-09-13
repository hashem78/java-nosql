package me.hashemalayan.util;

import btree4j.BTreeCallback;
import btree4j.Value;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.function.BiFunction;

public class BTreeCallbackFactory {

    final ObjectMapper objectMapper;

    @Inject
    BTreeCallbackFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BTreeCallback create(BiFunction<String, String, Boolean> func) {
        return new BTreeCallback() {
            @Override
            public boolean indexInfo(Value value, long pointer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean indexInfo(Value bTreeKey, byte[] bTreeValue) {
                try {
                    final var keyStr = objectMapper.readTree(bTreeKey.getData()).asText();
                    final var valueNode = objectMapper.readTree(bTreeValue);
                    return func.apply(keyStr, objectMapper.writeValueAsString(valueNode));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
