package me.hashemalayan.util;

import com.fasterxml.jackson.databind.JsonNode;

public record JsonIteratorResult(String documentName, JsonNode jsonNode) {
}
