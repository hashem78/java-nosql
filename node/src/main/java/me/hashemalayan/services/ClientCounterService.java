package me.hashemalayan.services;

import com.google.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientCounterService {

    private final AtomicInteger count;

    @Inject
    public ClientCounterService() {
        count = new AtomicInteger(0);
    }

    public int increment() {
        return count.incrementAndGet();
    }

    public int decrement() {
        return count.decrementAndGet();
    }

    public int get() {
        return count.get();
    }
}
