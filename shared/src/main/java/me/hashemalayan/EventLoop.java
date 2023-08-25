package me.hashemalayan;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class EventLoop {

    private final Map<Class<? extends Event>, Consumer<Event>> registry = new ConcurrentHashMap<>();
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    @Inject
    public EventLoop(Set<EventHandler<? extends Event>> handlers) {
        handlers.forEach(handler -> registry.put(handler.getHandledEventType(), event -> safeHandle(handler, event)));
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void safeHandle(EventHandler<T> handler, Event event) {
        handler.handle((T) event);
    }

    public void dispatch(Event event) {
        eventQueue.add(event);  // Or use eventQueue.put(event) if you want it to block when queue is full.
    }

    public void process() {
        while (true) {
            try {
                Event event = eventQueue.take(); // Blocks until an event is available.
                registry.getOrDefault(event.getClass(), e -> {}).accept(event);
            } catch (InterruptedException e) {
                // This thread was interrupted while waiting. You can handle this case appropriately.
                Thread.currentThread().interrupt(); // Preserve the interrupt status.
                break;
            }
        }
    }
}
