package me.hashemalayan.signaling.events;

import me.hashemalayan.Event;

public record DiscoverRemoteNodesEvent(String localPort) implements Event {
}
