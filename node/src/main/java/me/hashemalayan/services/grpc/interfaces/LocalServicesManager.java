package me.hashemalayan.services.grpc.interfaces;

import java.io.IOException;

public interface LocalServicesManager {
    void init() throws IOException;

    void awaitTermination() throws InterruptedException;

    void cleanup();
}
