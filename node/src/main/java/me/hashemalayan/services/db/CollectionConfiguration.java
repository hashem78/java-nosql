package me.hashemalayan.services.db;

import com.networknt.schema.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import me.hashemalayan.nosql.shared.CollectionMetaData;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionConfiguration {
    CollectionMetaData metaData;
    JsonSchema schema;
}
