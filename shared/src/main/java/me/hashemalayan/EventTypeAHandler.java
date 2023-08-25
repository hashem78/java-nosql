package me.hashemalayan;

public class EventTypeAHandler implements EventHandler<EventTypeA> {
    @Override
    public void handle(EventTypeA event) {
        System.out.println("Handling EventTypeA");
    }

    @Override
    public Class<EventTypeA> getHandledEventType() {
        return EventTypeA.class;
    }
}
