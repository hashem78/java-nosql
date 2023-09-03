package me.hashemalayan.services;

import com.google.inject.Inject;
import org.slf4j.Logger;

public class DatabaseService {

    private final Logger logger;

    @Inject
    public DatabaseService(Logger logger) {
        this.logger = logger;
    }
}
