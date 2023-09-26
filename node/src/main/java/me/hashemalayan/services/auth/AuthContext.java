package me.hashemalayan.services.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.protobuf.util.Timestamps;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.Common;
import me.hashemalayan.nosql.shared.Common.DocumentMetaData;
import me.hashemalayan.nosql.shared.Customstruct;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.nosql.shared.User;
import me.hashemalayan.services.auth.exceptions.UserAlreadyExistsException;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;

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

    public boolean userExists(String email, String password) {
        final var emailResults = databaseService.runQuery(
                "auth",
                Operator.EQUALS,
                "email",
                Customstruct.CustomValue.newBuilder()
                        .setStringValue(email)
                        .build()
        );
        final var passwordResults = databaseService.runQuery(
                "auth",
                Operator.EQUALS,
                "password",
                Customstruct.CustomValue.newBuilder()
                        .setStringValue(password)
                        .build()
        );
        return !emailResults.isEmpty() && !passwordResults.isEmpty();
    }

    public boolean userExists(String userId) {
        final var userIdResults = databaseService.runQuery(
                "auth",
                Operator.EQUALS,
                "userId",
                Customstruct.CustomValue.newBuilder()
                        .setStringValue(userId)
                        .build()
        );
        return !userIdResults.isEmpty();
    }

    public User registerUser(String email, String password) throws UserAlreadyExistsException {

        if (userExists(email, password)) {
            throw new UserAlreadyExistsException();
        }

        final var createdOn = Timestamps.fromMillis(System.currentTimeMillis());
        final var userId = UUID.randomUUID().toString();
        final var documentId = UUID.randomUUID().toString();

        final var userJsonNode = objectMapper.createObjectNode();

        userJsonNode.put("userId", userId);
        userJsonNode.put("email", email);
        userJsonNode.put("password", email);

        try {
            databaseService.setDocument(
                    "auth",
                    Common.CollectionDocument
                            .newBuilder()
                            .setMetaData(
                                    DocumentMetaData.newBuilder()
                                            .setCreatedOn(createdOn)
                                            .setLastEditedOn(createdOn)
                                            .setAffinity(nodeProperties.getPort())
                                            .setId(documentId)
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
