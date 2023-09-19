package me.hashemalayan.services;

import com.google.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

public class BasicClientCounterService implements ClientCounterService {

    private final AtomicInteger count;

    @Inject
    public BasicClientCounterService() {
        count = new AtomicInteger(0);
    }

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    @Override
    public void decrement() {
        count.decrementAndGet();
    }

    @Override
    public int get() {
        return count.get();
    }
}
