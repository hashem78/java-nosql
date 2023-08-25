package me.hashemalayan;

public interface EventHandler<T extends Event> {
    void handle(T event);
    Class<T> getHandledEventType();
}