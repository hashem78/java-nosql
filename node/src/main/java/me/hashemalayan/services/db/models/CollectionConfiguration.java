package me.hashemalayan.services.db.models;

import com.networknt.schema.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionConfiguration {
    CollectionMetaData metaData;
    JsonSchema schema;
}
