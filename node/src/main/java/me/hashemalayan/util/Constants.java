package me.hashemalayan.util;

import io.grpc.Context;
import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class Constants {
    public static final String Draft7MetaScheme = """
            {
                "$schema": "http://json-schema.org/draft-07/schema#",
                "$id": "http://json-schema.org/draft-07/schema#",
                "title": "Core schema meta-schema",
                "definitions": {
                    "schemaArray": {
                        "type": "array",
                        "minItems": 1,
                        "items": { "$ref": "#" }
                    },
                    "nonNegativeInteger": {
                        "type": "integer",
                        "minimum": 0
                    },
                    "nonNegativeIntegerDefault0": {
                        "allOf": [
                            { "$ref": "#/definitions/nonNegativeInteger" },
                            { "default": 0 }
                        ]
                    },
                    "simpleTypes": {
                        "enum": [
                            "array",
                            "boolean",
                            "integer",
                            "null",
                            "number",
                            "object",
                            "string"
                        ]
                    },
                    "stringArray": {
                        "type": "array",
                        "items": { "type": "string" },
                        "uniqueItems": true,
                        "default": []
                    }
                },
                "type": ["object", "boolean"],
                "properties": {
                    "$id": {
                        "type": "string",
                        "format": "uri-reference"
                    },
                    "$schema": {
                        "type": "string",
                        "format": "uri"
                    },
                    "$ref": {
                        "type": "string",
                        "format": "uri-reference"
                    },
                    "$comment": {
                        "type": "string"
                    },
                    "title": {
                        "type": "string"
                    },
                    "description": {
                        "type": "string"
                    },
                    "default": true,
                    "readOnly": {
                        "type": "boolean",
                        "default": false
                    },
                    "writeOnly": {
                        "type": "boolean",
                        "default": false
                    },
                    "examples": {
                        "type": "array",
                        "items": true
                    },
                    "multipleOf": {
                        "type": "number",
                        "exclusiveMinimum": 0
                    },
                    "maximum": {
                        "type": "number"
                    },
                    "exclusiveMaximum": {
                        "type": "number"
                    },
                    "minimum": {
                        "type": "number"
                    },
                    "exclusiveMinimum": {
                        "type": "number"
                    },
                    "maxLength": { "$ref": "#/definitions/nonNegativeInteger" },
                    "minLength": { "$ref": "#/definitions/nonNegativeIntegerDefault0" },
                    "pattern": {
                        "type": "string",
                        "format": "regex"
                    },
                    "additionalItems": { "$ref": "#" },
                    "items": {
                        "anyOf": [
                            { "$ref": "#" },
                            { "$ref": "#/definitions/schemaArray" }
                        ],
                        "default": true
                    },
                    "maxItems": { "$ref": "#/definitions/nonNegativeInteger" },
                    "minItems": { "$ref": "#/definitions/nonNegativeIntegerDefault0" },
                    "uniqueItems": {
                        "type": "boolean",
                        "default": false
                    },
                    "contains": { "$ref": "#" },
                    "maxProperties": { "$ref": "#/definitions/nonNegativeInteger" },
                    "minProperties": { "$ref": "#/definitions/nonNegativeIntegerDefault0" },
                    "required": { "$ref": "#/definitions/stringArray" },
                    "additionalProperties": { "$ref": "#" },
                    "definitions": {
                        "type": "object",
                        "additionalProperties": { "$ref": "#" },
                        "default": {}
                    },
                    "properties": {
                        "type": "object",
                        "additionalProperties": { "$ref": "#" },
                        "default": {}
                    },
                    "patternProperties": {
                        "type": "object",
                        "additionalProperties": { "$ref": "#" },
                        "propertyNames": { "format": "regex" },
                        "default": {}
                    },
                    "dependencies": {
                        "type": "object",
                        "additionalProperties": {
                            "anyOf": [
                                { "$ref": "#" },
                                { "$ref": "#/definitions/stringArray" }
                            ]
                        }
                    },
                    "propertyNames": { "$ref": "#" },
                    "const": true,
                    "enum": {
                        "type": "array",
                        "items": true,
                        "minItems": 1,
                        "uniqueItems": true
                    },
                    "type": {
                        "anyOf": [
                            { "$ref": "#/definitions/simpleTypes" },
                            {
                                "type": "array",
                                "items": { "$ref": "#/definitions/simpleTypes" },
                                "minItems": 1,
                                "uniqueItems": true
                            }
                        ]
                    },
                    "format": { "type": "string" },
                    "contentMediaType": { "type": "string" },
                    "contentEncoding": { "type": "string" },
                    "if": { "$ref": "#" },
                    "then": { "$ref": "#" },
                    "else": { "$ref": "#" },
                    "allOf": { "$ref": "#/definitions/schemaArray" },
                    "anyOf": { "$ref": "#/definitions/schemaArray" },
                    "oneOf": { "$ref": "#/definitions/schemaArray" },
                    "not": { "$ref": "#" }
                },
                "default": true
            }
            """;
    public static String authSchema = """
            {
              "type": "object",
              "properties": {
                "userId": {
                  "type": "string"
                },
                "email": {
                  "type": "string"
                },
                "password": {
                  "type": "string"
                }
              }
            }
            """;
    public static final String JWT_SIGNING_KEY = "L8hHXsaQOUjk5rg7XPGv4eL36anlCrkMz8CJ0i/8E/0=";
    public static final String BEARER_TYPE = "Bearer";
    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of(
            "Authorization",
            ASCII_STRING_MARSHALLER
    );
    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("userId");
}