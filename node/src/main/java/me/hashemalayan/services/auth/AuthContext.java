package me.hashemalayan.services.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.protobuf.util.Timestamps;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.Common;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.DocumentMetaData;
import me.hashemalayan.nosql.shared.Customstruct;
import me.hashemalayan.nosql.shared.Customstruct.CustomStruct;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.nosql.shared.User;
import me.hashemalayan.services.auth.exceptions.UserAlreadyExistsException;
import me.hashemalayan.services.auth.exceptions.UserDoesNotExistException;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;

import java.io.FileDescriptor;
import java.util.Optional;
import java.util.UUID;

public class AuthContext {
    private final AbstractDatabaseService databaseService;
    private final ObjectMapper objectMapper;
    private final NodeProperties nodeProperties;

    @Inject
    public AuthContext(
            @Named("BasicDbService") AbstractDatabaseService databaseService,
            ObjectMapper objectMapper,
            NodeProperties nodeProperties
    ) {
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
        this.nodeProperties = nodeProperties;
    }

    private Optional<String> queryAuth(String email, String password) {
        final var result = databaseService.compoundQuery(
                "auth",
                Operator.EQUALS,
                CustomStruct.newBuilder()
                        .putFields(
                                "email",
                                Customstruct.CustomValue.newBuilder()
                                        .setStringValue(email)
                                        .build()
                        )
                        .putFields(
                                "password",
                                Customstruct.CustomValue.newBuilder()
                                        .setStringValue(password)
                                        .build()
                        )
                        .build()
        );
        if (result.isEmpty())
            return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(result.get(0), String.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public User getCredentials(String email, String password) {
        final var docId = queryAuth(email, password);
        if (docId.isEmpty()) {
            throw new UserDoesNotExistException();
        }
        try {
            final var doc = databaseService.getDocument("auth", docId.get());
            final var docJson = objectMapper.readTree(doc.getData());
            return User.newBuilder()
                    .setEmail(docJson.get("email").asText())
                    .setUserId(docJson.get("userId").asText())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public User registerUser(String email, String password) throws UserAlreadyExistsException {

        if (queryAuth(email, password).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        final var createdOn = Timestamps.fromMillis(System.currentTimeMillis());
        final var userId = UUID.randomUUID().toString();

        final var userJsonNode = objectMapper.createObjectNode();

        userJsonNode.put("userId", userId);
        userJsonNode.put("email", email);
        userJsonNode.put("password", password);

        try {
            databaseService.setDocument(
                    "auth",
                    CollectionDocument
                            .newBuilder()
                            .setMetaData(
                                    DocumentMetaData.newBuilder()
                                            .setCreatedOn(createdOn)
                                            .setLastEditedOn(createdOn)
                                            .setAffinity(nodeProperties.getPort())
                                            .setId(userId)
                                            .setDeleted(false)
                                            .build()
                            )
                            .setData(objectMapper.writeValueAsString(userJsonNode))
                            .build()
            );

            return User.newBuilder()
                    .setUserId(userId)
                    .setEmail(email)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
