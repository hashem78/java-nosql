package me.hashemalayan.db;

import com.google.inject.Inject;
import org.slf4j.Logger;

public class DBManager {

    private final Logger logger;

    @Inject
    public DBManager(Logger logger) {
        this.logger = logger;
    }
}
