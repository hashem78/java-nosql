package me.hashemalayan.services;

public interface ClientCounterService {
    void increment();

    void decrement();

    int get();
}
