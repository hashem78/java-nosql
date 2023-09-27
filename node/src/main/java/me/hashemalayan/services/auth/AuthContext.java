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
import me.hashemalayan.services.auth.exceptions.UserDoesNotExistException;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;

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

    public Optional<String> getUserId(String email, String password) {
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
        if (!emailResults.isEmpty() && !passwordResults.isEmpty()) {
            return Optional.of(passwordResults.get(0));
        }
        return Optional.empty();
    }

    public User getCredentials(String email, String password) {
        final var userIdOpt = getUserId(email, password);
        if (userIdOpt.isEmpty()) {
            throw new UserDoesNotExistException();
        }
        try {
            final var doc = databaseService.getDocument("auth", userIdOpt.get());
            final var docJson = objectMapper.readTree(doc.getData());
            return User.newBuilder()
                    .setEmail(docJson.get("email").asText())
                    .setUserId(userIdOpt.get())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public User registerUser(String email, String password) throws UserAlreadyExistsException {

        if (getUserId(email, password).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        final var createdOn = Timestamps.fromMillis(System.currentTimeMillis());
        final var userId = UUID.randomUUID().toString();

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
