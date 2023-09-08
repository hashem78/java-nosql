package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SampleFromSchemaServiceTest {
    final ObjectMapper objectMapper = new ObjectMapper();
    final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    final SampleFromSchemaService service = new SampleFromSchemaService(objectMapper);

    @Test
    void build() {

        final var schema = jsonSchemaFactory.getSchema("""
                {
                    "type" : "object",
                    "properties" : {
                      "metaData" : {
                        "type" : "object",
                        "properties" : {
                          "id" : {
                            "type" : "string"
                          },
                          "createdOn" : {
                            "type" : "string"
                          }
                        },
                        "required" : [ "id", "createdOn" ]
                      },
                      "data" : {
                        "type" : "object",
                        "properties" : {
                          "name" : {
                            "type" : "string"
                          },
                          "age" : {
                            "type" : "number"
                          }
                        },
                        "required" : [ "name", "age" ]
                      }
                    },
                    "required" : [ "metaData", "data" ]
                  }
                """);

        var results = service.getSample("temp", schema.getSchemaNode());
        System.out.println(results);
        Assertions.assertEquals(
                """
                        {"metaData":{"id":"","createdOn":""},"data":{"name":"","age":0}}
                        """.strip(),
                results.toString()
        );
    }
}