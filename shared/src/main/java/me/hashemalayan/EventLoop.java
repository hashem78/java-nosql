package me.hashemalayan;

import com.google.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.Set;

public class EventLoop {

    private final Map<Class<? extends Event>, Consumer<Event>> registry = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<>();

    @Inject
    public EventLoop(Set<EventHandler<? extends Event>> handlers) {
        handlers.forEach(handler -> registry.put(handler.getHandledEventType(), event -> safeHandle(handler, event)));
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void safeHandle(EventHandler<T> handler, Event event) {
        handler.handle((T) event);
    }

    public void dispatch(Event event) {
        eventQueue.add(event);
    }

    public void process() {
        Event event;
        while ((event = eventQueue.poll()) != null) {
            registry.getOrDefault(event.getClass(), e -> {}).accept(event);
        }
    }
}
